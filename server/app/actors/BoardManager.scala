package actors

import scala.concurrent.ExecutionContext
import scala.collection.mutable
import akka.actor.{Actor, ActorRef, Props}
import models.{BoardModel, UserModel, Participant}
import models.protocols.BoardProtocol._

class BoardManager(boardModel: BoardModel, userModel: UserModel)
    (implicit ec: ExecutionContext) extends Actor {

  private val actors = mutable.Map[String, List[ActorRef]]()

  def receive = {

    case (actor: ActorRef, NewSpectator(boardId)) => {
      actors += boardId -> (actor +: actors.get(boardId).getOrElse(Nil))
      boardModel.getBoard(boardId).foreach(actor ! SetBoard(_))
      boardModel.getParticipants(boardId).map(actor ! SetPlayers(_))
    }

    case (actor: ActorRef, JoinGame(boardId, userId)) =>
      for {
        _ <- boardModel.joinBoard(boardId, userId)
        participants <- boardModel.getParticipants(boardId)
      } broadcast(boardId, SetPlayers(participants))
    
    case (actor: ActorRef, RemovePlayer(boardId, playerId)) => 
      for {
        _ <- boardModel.removePlayer(boardId, playerId)
        participants <- boardModel.getParticipants(boardId)
      } broadcast(boardId, SetPlayers(participants))

    case (actor: ActorRef, PromotePlayer(boardId, playerId)) =>
      for {
        promoted <- boardModel.promotePlayer(boardId, playerId)
        participants <- boardModel.getParticipants(boardId)
      } if (promoted) broadcast(boardId, SetPlayers(participants))

    case (actor: ActorRef, DemotePlayer(boardId, playerId)) =>
      for {
        demoted <- boardModel.demotePlayer(boardId, playerId)
        participants <- boardModel.getParticipants(boardId)
      } if (demoted) broadcast(boardId, SetPlayers(participants))
    
    case (actor: ActorRef, DeleteGame(boardId)) => {
      boardModel.deleteBoard(boardId)
      broadcast(boardId, SetBoard(None))
    }

    case (actor: ActorRef, StartGame(boardId)) => {
      for {
        started <- boardModel.startGame(boardId)
        board <- boardModel.getBoard(boardId)
      } if (started) broadcast(boardId, SetBoard(board))
    }
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