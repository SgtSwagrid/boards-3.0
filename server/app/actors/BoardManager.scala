package actors

import scala.concurrent.ExecutionContext
import scala.collection.mutable
import akka.actor.{Actor, ActorRef, Props}
import models.{BoardModel, UserModel, Participant}
import requests.BoardRequests._

class BoardManager(boardModel: BoardModel, userModel: UserModel)
    (implicit ec: ExecutionContext) extends Actor {

  private val actors = mutable.Map[String, List[ActorRef]]()

  def receive = {

    case (actor: ActorRef, NewSpectator(boardId, _)) => {
      actors += boardId -> (actor +: actors.get(boardId).getOrElse(Nil))
      boardModel.getParticipants(boardId).map(actor ! SetPlayers(_))
    }

    case (actor: ActorRef, JoinGame(boardId, userId, _)) =>
      for {
        _ <- boardModel.joinBoard(boardId, userId)
        participants <- boardModel.getParticipants(boardId)
      } broadcast(boardId, SetPlayers(participants))
    
    case (actor: ActorRef, LeaveGame(boardId, userId, _)) => 
      for {
        _ <- boardModel.leaveBoard(boardId, userId)
        participants <- boardModel.getParticipants(boardId)
      } broadcast(boardId, SetPlayers(participants))
  }

  private def broadcast(boardId: String, res: BoardResponse) = {
    actors.get(boardId).getOrElse(Nil).foreach(_ ! res)
  }
}

object BoardManager {

  def props(boardModel: BoardModel, userModel: UserModel)
      (implicit ec: ExecutionContext) =
    Props(new BoardManager(boardModel, userModel))
}