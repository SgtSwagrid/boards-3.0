package actors

import akka.actor.{Actor, ActorRef, Props}
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import models.protocols.BoardProtocol._, models.User

class BoardActor (
  out: ActorRef,
  manager: ActorRef,
  boardId: String,
  userId: Int
) extends Actor {

  manager ! (self, userId, NewSpectator)

  def receive = {
    case req: String => decode[BoardRequest](req) match {
      case Right(action) => manager ! (self, userId, action)
      case Left(_) => println(s"Could not decode JSON:\n$req")
    }
    case res: BoardResponse => out ! res.asJson.toString
  }
}

object BoardActor {

  def props(out: ActorRef, manager: ActorRef, boardId: String, userId: Int) =
    Props(new BoardActor(out, manager, boardId, userId))
}