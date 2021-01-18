package actors

import akka.actor.{Actor, ActorRef, Props}
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import models.protocols.BoardProtocol._, models.User

class BoardActor(out: ActorRef, manager: ActorRef, boardId: String) extends Actor {

  manager ! (self, NewSpectator(boardId))

  def receive = {
    case req: String => decode[BoardRequest](req) match {
      case Right(action) => manager ! (self, action)
      case Left(_) => {}
    }
    case res: BoardResponse => out ! res.asJson.toString
  }
}

object BoardActor {

  def props(out: ActorRef, manager: ActorRef, boardId: String) =
    Props(new BoardActor(out, manager, boardId))
}