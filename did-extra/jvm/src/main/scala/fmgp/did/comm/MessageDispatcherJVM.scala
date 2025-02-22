package fmgp.did.comm

import scala.util.chaining._

import zio._
import zio.json._
import zio.http.{MediaType => ZMediaType, _}

import fmgp.did._
import fmgp.did.comm._
import fmgp.crypto.error._
import fmgp.util.MyHeaders

object MessageDispatcherJVM {
  val layer: ZLayer[Client, Throwable, MessageDispatcher] =
    ZLayer.fromZIO(
      ZIO
        .service[Client]
        .map(MessageDispatcherJVM(_))
    )
}

class MessageDispatcherJVM(client: Client) extends MessageDispatcher {
  def send(
      msg: EncryptedMessage,
      /*context*/
      destination: String,
      xForwardedHost: Option[String],
  ): ZIO[Any, DidFail, String] = {
    val contentTypeHeader = msg.`protected`.obj.typ
      .getOrElse(MediaTypes.ENCRYPTED)
      .pipe(e => Header.ContentType(ZMediaType(e.mainType, e.subType)))
    val xForwardedHostHeader = xForwardedHost.map(x => Header.Custom(customName = MyHeaders.xForwardedHost, x))

    for {
      res <- Client
        .request(
          url = destination,
          method = Method.POST,
          headers = Headers(Seq(Some(contentTypeHeader), xForwardedHostHeader).flatten),
          content = Body.fromCharSequence(msg.toJson),
        )
        .tapError(ex => ZIO.logWarning(s"Fail when calling '$destination': ${ex.toString}"))
        .mapError(ex => SomeThrowable(ex))
      data <- res.body.asString
        .tapError(ex => ZIO.logError(s"Fail parce http response body: ${ex.toString}"))
        .mapError(ex => SomeThrowable(ex))
      _ <- res.status.isError match
        case true  => ZIO.logError(data)
        case false => ZIO.logInfo(data)
    } yield (data)
  }.provideEnvironment(ZEnvironment(client)) // .host()
}
