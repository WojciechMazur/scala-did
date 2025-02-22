package fmgp.did.comm.mediator

import zio._
import zio.json._
import zio.http._
import zio.http.internal.middlewares.Cors.CorsConfig
import zio.http.Header.AccessControlAllowMethods
import zio.http.Header.AccessControlAllowOrigin

import io.netty.handler.codec.http.HttpHeaderNames

import fmgp.util._
import fmgp.did._
import fmgp.crypto._
import fmgp.crypto.error._
import fmgp.did.comm._
import fmgp.did.comm.protocol._

case class MediatorAgent(
    override val id: DID,
    override val keyStore: KeyStore, // Shound we make it lazy with ZIO
    didSocketManager: Ref[DIDSocketManager],
    messageDB: Ref[MessageDB],
) extends Agent {
  override def keys: Seq[PrivateKey] = keyStore.keys.toSeq

  // val resolverLayer: ULayer[DynamicResolver] =
  //   DynamicResolver.resolverLayer(didSocketManager)

  type Services = Resolver & Agent & Operations & MessageDispatcher & Ref[MediatorDB]
  val protocolHandlerLayer: URLayer[Ref[MediatorDB], ProtocolExecuter[Services]] =
    ZLayer {
      for {
        ref <- ZIO.service[Ref[MediatorDB]]
      } yield ProtocolExecuterCollection[Services](
        BasicMessageExecuter,
        new TrustPingExecuter,
        MediatorCoordinationExecuter,
        ForwardMessageExecuter,
        PickupExecuter,
      )
    }

  // private def _didSubjectAux = id
  // private def _keyStoreAux = keyStore.keys.toSeq
  // val indentityLayer = ZLayer.succeed(new Agent {
  //   override def id: DID = _didSubjectAux
  //   override def keys: Seq[PrivateKey] = _keyStoreAux
  // })

  val messageDispatcherLayer: ZLayer[Client, DidFail, MessageDispatcher] =
    MessageDispatcherJVM.layer.mapError(ex => SomeThrowable(ex))

  // TODO move to another place & move validations and build a contex
  def decrypt(msg: Message): ZIO[Agent & Resolver & Operations, DidFail, PlaintextMessage] =
    for {
      ops <- ZIO.service[Operations]
      plaintextMessage <- msg match
        case pm: PlaintextMessage => ZIO.succeed(pm)
        case em: EncryptedMessage =>
          {
            em.`protected`.obj match
              case AnonProtectedHeader(epk, apv, typ, enc, alg)            => ops.anonDecrypt(em)
              case AuthProtectedHeader(epk, apv, skid, apu, typ, enc, alg) => ops.authDecrypt(em)
          }.flatMap(decrypt _)
        case sm: SignedMessage =>
          ops.verify(sm).flatMap {
            case false => ZIO.fail(ValidationFailed)
            case true =>
              sm.payload.content.fromJson[Message] match
                case Left(error) => ZIO.fail(FailToParse(error))
                case Right(msg2) => decrypt(msg2)
          }
    } yield (plaintextMessage)

  def receiveMessage(
      data: String,
      mSocketID: Option[SocketID],
  ): ZIO[
    Operations & Resolver & MessageDispatcher & MediatorAgent & Ref[MediatorDB],
    DidFail,
    Option[EncryptedMessage]
  ] =
    for {
      msg <- data.fromJson[EncryptedMessage] match
        case Left(error) =>
          ZIO.logError(s"Data is not a EncryptedMessage: $error")
            *> ZIO.fail(FailToParse(error))
        case Right(message) =>
          ZIO.logDebug(
            "Message's recipients KIDs: " + message.recipientsKid.mkString(",") +
              "; DID: " + "Message's recipients DIDs: " + message.recipientsSubject.mkString(",")
          ) *> ZIO.succeed(message)
      maybeSyncReplyMsg <- receiveMessage(msg, mSocketID)
    } yield (maybeSyncReplyMsg)

  def receiveMessage(
      msg: EncryptedMessage,
      mSocketID: Option[SocketID]
  ): ZIO[
    Operations & Resolver & MessageDispatcher & MediatorAgent & Ref[MediatorDB],
    DidFail,
    Option[EncryptedMessage]
  ] =
    ZIO
      .logAnnotate("msgHash", msg.hashCode.toString) {
        for {
          _ <- ZIO.log(s"receiveMessage with hashCode: ${msg.hashCode}")
          maybeSyncReplyMsg <-
            if (!msg.recipientsSubject.contains(id))
              ZIO.logError(s"This mediator '${id.string}' is not a recipient")
                *> ZIO.none
            else
              for {
                _ <- messageDB.update(db => db.add(msg))
                plaintextMessage <- decrypt(msg)
                _ <- didSocketManager.get.flatMap { m => // TODO HACK REMOVE !!!!!!!!!!!!!!!!!!!!!!!!
                  ZIO.foreach(m.tapSockets)(_.socketOutHub.publish(TapMessage(msg, plaintextMessage).toJson))
                }
                _ <- mSocketID match
                  case None => ZIO.unit
                  case Some(socketID) =>
                    plaintextMessage.from match
                      case None       => ZIO.unit
                      case Some(from) => didSocketManager.update { _.link(from.asFROMTO, socketID) }
                // TODO Store context of the decrypt unwarping
                // TODO SreceiveMessagetore context with MsgID and PIURI
                protocolHandler <- ZIO.service[ProtocolExecuter[Services]]
                ret <- protocolHandler
                  .execute(plaintextMessage)
                  .tapError(ex => ZIO.logError(s"Error when execute Protocol: $ex"))
              } yield ret
        } yield maybeSyncReplyMsg
      }
      .provideSomeLayer( /*resolverLayer ++ indentityLayer ++*/ protocolHandlerLayer)

  def createSocketApp(
      annotationMap: Seq[LogAnnotation]
  ): ZIO[MediatorAgent & Resolver & Operations & MessageDispatcher & Ref[MediatorDB], Nothing, zio.http.Response] = {
    import zio.http.ChannelEvent._
    val SOCKET_ID = "SocketID"
    Handler
      .webSocket { channel => // WebSocketChannel = Channel[ChannelEvent[WebSocketFrame], ChannelEvent[WebSocketFrame]]
        val channelId = scala.util.Random.nextLong().toString
        channel.receiveAll {
          case UserEventTriggered(UserEvent.HandshakeComplete) =>
            ZIO.logAnnotate(LogAnnotation(SOCKET_ID, channelId), annotationMap: _*) {
              ZIO.log(s"HandshakeComplete $channelId") *>
                DIDSocketManager.registerSocket(channel, channelId)
            }
          case UserEventTriggered(UserEvent.HandshakeTimeout) =>
            ZIO.logAnnotate(LogAnnotation(SOCKET_ID, channelId), annotationMap: _*) {
              ZIO.logWarning(s"HandshakeTimeout $channelId")
            }
          case ChannelEvent.Registered =>
            ZIO.logAnnotate(LogAnnotation(SOCKET_ID, channelId), annotationMap: _*) {
              DIDSocketManager.registerSocket(channel, channelId)
            }
          case ChannelEvent.Unregistered =>
            ZIO.logAnnotate(LogAnnotation(SOCKET_ID, channelId), annotationMap: _*) {
              DIDSocketManager.unregisterSocket(channel, channelId)
            }
          case ChannelEvent.Read(WebSocketFrame.Text(text)) =>
            ZIO.logAnnotate(LogAnnotation(SOCKET_ID, channelId), annotationMap: _*) {
              DIDSocketManager
                .newMessage(channel, text, channelId)
                .flatMap { case (socketID, encryptedMessage) => receiveMessage(encryptedMessage, Some(socketID)) }
                .mapError(ex => DidException(ex))
            }
          case ChannelEvent.Read(any) =>
            ZIO.logAnnotate(LogAnnotation(SOCKET_ID, channelId), annotationMap: _*) {
              ZIO.logError(s"Unknown event type from '${channelId}': " + any.getClass())
            }
          case ChannelEvent.ExceptionCaught(ex) =>
            ZIO.logAnnotate(LogAnnotation(SOCKET_ID, channelId), annotationMap: _*) {
              ZIO.log(ex.getMessage())
            }
        }
      }
      .toResponse
      .provideSomeEnvironment { (env) => env.add(env.get[MediatorAgent].didSocketManager) }
  }

  def websocketListenerApp(
      annotationMap: Seq[LogAnnotation]
  ): ZIO[MediatorAgent & Operations & MessageDispatcher, Nothing, zio.http.Response] = {
    import zio.http.ChannelEvent._
    val SOCKET_ID = "SocketID"
    Handler
      .webSocket { channel => // WebSocketChannel = Channel[ChannelEvent[WebSocketFrame], ChannelEvent[WebSocketFrame]]
        val channelId = scala.util.Random.nextLong().toString
        channel.receiveAll {
          case UserEventTriggered(UserEvent.HandshakeComplete) =>
            ZIO.logAnnotate(LogAnnotation(SOCKET_ID, channelId), annotationMap: _*) {
              ZIO.log(s"HandshakeComplete $channelId") *>
                DIDSocketManager.tapSocket(id, channel, channelId)
            }
          case UserEventTriggered(UserEvent.HandshakeTimeout) =>
            ZIO.logAnnotate(LogAnnotation(SOCKET_ID, channelId), annotationMap: _*) {
              ZIO.logWarning(s"HandshakeTimeout $channelId")
            }
          case ChannelEvent.Registered =>
            ZIO.logAnnotate(LogAnnotation(SOCKET_ID, channelId), annotationMap: _*) {
              DIDSocketManager.registerSocket(channel, channelId)
            }
          case ChannelEvent.Unregistered =>
            ZIO.logAnnotate(LogAnnotation(SOCKET_ID, channelId), annotationMap: _*) {
              DIDSocketManager.unregisterSocket(channel, channelId)
            }
          case ChannelEvent.Read(any) =>
            ZIO.logAnnotate(LogAnnotation(SOCKET_ID, channelId), annotationMap: _*) {
              ZIO.logWarning(s"Ignored Message from '${channelId}'")
            }
          case ChannelEvent.ExceptionCaught(ex) =>
            ZIO.logAnnotate(LogAnnotation(SOCKET_ID, channelId), annotationMap: _*) {
              ZIO.log(ex.getMessage())
            }
        }
      }
      .toResponse
      .provideSomeEnvironment { (env) => env.add(env.get[MediatorAgent].didSocketManager) }
  }
}

object MediatorAgent {

  def make(id: DID, keyStore: KeyStore): ZIO[Any, Nothing, MediatorAgent] = for {
    sm <- DIDSocketManager.make
    db <- Ref.make(MessageDB())
  } yield MediatorAgent(id, keyStore, sm, db)

  // def make(agent: AgentDIDPeer): ZIO[Any, Nothing, MediatorAgent] = for {
  //   sm <- DIDSocketManager.make
  //   db <- Ref.make(MessageDB())
  // } yield MediatorAgent(agent.id, agent.keyStore, sm, db)

  def didCommApp = {
    Http.collectZIO[Request] {
      case req @ Method.GET -> Root
          if req
            // FIXME after https://github.com/zio/zio-http/issues/2416
            // .header(Header.ContentType)
            // .exists { h =>
            //   h.mediaType.mainType == MediaTypes.mainType &&
            //   (h.mediaType.subType == MediaTypes.SIGNED.subType || h.mediaType.subType == MediaTypes.ENCRYPTED.subType)
            .headers
            .get("content-type")
            .exists { h =>
              h == MediaTypes.SIGNED.typ || h == MediaTypes.ENCRYPTED.typ
            } =>
        for {
          agent <- ZIO.service[MediatorAgent]
          annotationMap <- ZIO.logAnnotations.map(_.map(e => LogAnnotation(e._1, e._2)).toSeq)
          ret <- agent.createSocketApp(annotationMap)
        } yield (ret)
      case Method.GET -> Root / "tap" => // TODO only on dev mode
        for {
          agent <- ZIO.service[MediatorAgent]
          annotationMap <- ZIO.logAnnotations.map(_.map(e => LogAnnotation(e._1, e._2)).toSeq)
          ret <- agent.websocketListenerApp(annotationMap)
        } yield (ret)
      case req @ Method.POST -> Root
          if req
            // FIXME after https://github.com/zio/zio-http/issues/2416
            // .header(Header.ContentType)
            // .exists { h =>
            //   h.mediaType.mainType == MediaTypes.mainType &&
            //   (h.mediaType.subType == MediaTypes.SIGNED.subType || h.mediaType.subType == MediaTypes.ENCRYPTED.subType)
            .headers
            .get("content-type")
            .exists { h =>
              h == MediaTypes.SIGNED.typ || h == MediaTypes.ENCRYPTED.typ
            } =>
        for {
          agent <- ZIO.service[MediatorAgent]
          data <- req.body.asString
          maybeSyncReplyMsg <- agent
            .receiveMessage(data, None)
            .mapError(fail => DidException(fail))
          ret = maybeSyncReplyMsg match
            case None        => Response.ok
            case Some(value) => Response.json(value.toJson)
        } yield ret

      // TODO [return_route extension](https://github.com/decentralized-identity/didcomm-messaging/blob/main/extensions/return_route/main.md)
      case req @ Method.POST -> Root =>
        for {
          agent <- ZIO.service[MediatorAgent]
          data <- req.body.asString
          ret <- agent
            .receiveMessage(data, None)
            .mapError(fail => DidException(fail))
        } yield Response
          .text(s"The content-type must be ${MediaTypes.SIGNED.typ} or ${MediaTypes.ENCRYPTED.typ}")
      // .copy(status = Status.BadRequest) but ok for now

    }: Http[
      Operations & Resolver & MessageDispatcher & MediatorAgent & Ref[MediatorDB],
      Throwable,
      Request,
      Response
    ]
  } @@
    HttpAppMiddleware.cors(
      CorsConfig(
        allowedOrigin = {
          // case origin @ Origin.Value(_, host, _) if host == "dev" => Some(AccessControlAllowOrigin.Specific(origin))
          case _ => Some(AccessControlAllowOrigin.All)
        },
        allowedMethods = AccessControlAllowMethods(Method.GET, Method.POST, Method.OPTIONS),
      )
    )

}
