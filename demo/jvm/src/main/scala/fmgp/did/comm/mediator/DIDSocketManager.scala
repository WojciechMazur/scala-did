package fmgp.did.comm.mediator

import zio._
import zio.json._
import zio.http._
import zio.stream._

import fmgp.did._
import fmgp.did.comm._
import fmgp.crypto.error._
import zio.http.ChannelEvent.Read

type SocketID = String
case class MyChannel(id: SocketID, socketOutHub: Hub[String])
case class DIDSocketManager(
    sockets: Map[SocketID, MyChannel] = Map.empty,
    ids: Map[FROMTO, Seq[SocketID]] = Map.empty,
    kids: Map[VerificationMethodReferenced, Seq[SocketID]] = Map.empty,
    tapBy: Seq[SocketID] = Seq.empty,
) {

  def link(from: VerificationMethodReferenced, socketID: SocketID): DIDSocketManager =
    if (!sockets.keySet.contains(socketID)) this // if sockets is close
    else
      kids.get(from) match
        case Some(seq) if seq.contains(socketID) => this
        case Some(seq) => this.copy(kids = kids + (from -> (seq :+ socketID))).link(from.fromto, socketID)
        case None      => this.copy(kids = kids + (from -> Seq(socketID))).link(from.fromto, socketID)

  def link(from: FROMTO, socketID: SocketID): DIDSocketManager =
    if (!sockets.keySet.contains(socketID)) this // if sockets is close
    else
      ids.get(from) match
        case Some(seq) if seq.contains(socketID) => this
        case Some(seq)                           => this.copy(ids = ids + (from -> (seq :+ socketID)))
        case None                                => this.copy(ids = ids + (from -> Seq(socketID)))

  def tap(socketID: SocketID) = this.copy(tapBy = tapBy :+ socketID)

  def tapSockets = sockets.filter(e => tapBy.contains(e._1)).map(_._2)

  def registerSocket(myChannel: MyChannel) = this.copy(sockets = sockets + (myChannel.id -> myChannel))

  def unregisterSocket(socketID: SocketID) = this.copy(
    sockets = sockets.view.filterKeys(_ != socketID).toMap,
    ids = ids.map { case (did, socketsID) => (did, socketsID.filter(_ != socketID)) }.filterNot(_._2.isEmpty),
    kids = kids.map { case (kid, socketsID) => (kid, socketsID.filter(_ != socketID)) }.filterNot(_._2.isEmpty),
    tapBy = tapBy.filter(_ != socketID)
  )

}

object DIDSocketManager {
  def inBoundSize = 5
  def outBoundSize = 3

  private given JsonEncoder[Hub[String]] = JsonEncoder.string.contramap((e: Hub[String]) => "HUB")
  private given JsonEncoder[MyChannel] = DeriveJsonEncoder.gen[MyChannel]
  given encoder: JsonEncoder[DIDSocketManager] = DeriveJsonEncoder.gen[DIDSocketManager]

  def make = Ref.make(DIDSocketManager())

  def tapSocket(didSubject: DIDSubject, channel: WebSocketChannel, channelId: String) =
    for {
      socketManager <- ZIO.service[Ref[DIDSocketManager]]
      hub <- Hub.bounded[String](outBoundSize)
      myChannel = MyChannel(channelId, hub)
      _ <- socketManager.update { _.registerSocket(myChannel).tap(socketID = channelId) }
      sink = ZSink.foreach((value: String) => channel.send(Read(WebSocketFrame.text(value))))
      _ <- ZIO.log(s"Tapping into channel")
      _ <- ZStream.fromHub(myChannel.socketOutHub).run(sink) // TODO .fork does not work!!!
      _ <- ZIO.log(s"Tap channel concluded")
    } yield ()

  def registerSocket(channel: WebSocketChannel, channelId: String) =
    for {
      socketManager <- ZIO.service[Ref[DIDSocketManager]]
      hub <- Hub.bounded[String](outBoundSize)
      myChannel = MyChannel(channelId, hub)
      _ <- socketManager.update { _.registerSocket(myChannel) }
      sink = ZSink.foreach((value: String) => channel.send(Read(WebSocketFrame.text(value))))
      _ <- ZIO.log(s"Registering channel")
      _ <- ZStream.fromHub(myChannel.socketOutHub).run(sink) // TODO .fork does not work!!!
      _ <- ZIO.log(s"Channel concluded")
    } yield ()

  def newMessage(channel: WebSocketChannel, data: String, channelId: String) =
    for {
      socketManager <- ZIO.service[Ref[DIDSocketManager]]
    } yield (channelId, data)

  def unregisterSocket(channel: WebSocketChannel, channelId: String) =
    for {
      socketManager <- ZIO.service[Ref[DIDSocketManager]]
      _ <- socketManager.update { case sm: DIDSocketManager => sm.unregisterSocket(channelId) }
      _ <- ZIO.log(s"Channel unregisterSocket")
    } yield ()

}
