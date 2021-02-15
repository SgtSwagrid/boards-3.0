package actors

import scala.concurrent.ExecutionContext
import scala.collection.mutable
import slick.jdbc.MySQLProfile.api.Database
import akka.actor.{Actor, ActorRef, Props}
import models.{BoardModel, UserModel}
import models.protocols.BoardProtocol._
import scala.concurrent.Future

class BoardManager(db: Database)
    (implicit ec: ExecutionContext) extends Actor {

  private val boards = new BoardModel(db)
  private val users = new UserModel(db)

  private val actors = mutable.Map[String, List[ActorRef]]()

  def receive = { case (actor: ActorRef, userId: Int, msg: BoardRequest) =>
    
    for {
      board <- boards.getBoard(msg.boardId)
      player <- boards.getPlayerByUser(msg.boardId, userId)
    } msg match {

      case NewSpectator(boardId) => {

        actors += boardId -> (actor +: actors.get(boardId).getOrElse(Nil))

        boards.getBoard(boardId).foreach(actor ! SetBoard(_))

        boards.getPlayersWithUsers(boardId)
          .map { case (p, u) => actor ! SetPlayers(p, u) }

        boards.getActions(boardId).map(a => actor !
          PushActions(a.map(a => ActionId(a.action, a.turnOrder))))
      }

      case JoinGame(boardId, joiningId) =>

        if (userId == joiningId)

          for {
            _ <- boards.joinBoard(boardId, joiningId)
            (players, users) <- boards.getPlayersWithUsers(boardId)
            
          } broadcast(boardId, SetPlayers(players, users))
      
      case RemovePlayer(boardId, playerId) =>

        if (player.exists(p => p.isOwner || p.id == playerId))

          for {
            _ <- boards.removePlayer(boardId, playerId)
            (players, users) <- boards.getPlayersWithUsers(boardId)

          } if (players.nonEmpty) {
            broadcast(boardId, SetPlayers(players, users))
          } else broadcast(boardId, SetBoard(None))

      case PromotePlayer(boardId, playerId) =>

        if (player.exists(_.isOwner))

          for {
            promoted <- boards.promotePlayer(boardId, playerId)
            (players, users) <- boards.getPlayersWithUsers(boardId)

          } broadcast(boardId, SetPlayers(players, users))

      case DemotePlayer(boardId, playerId) =>

        if (player.exists(_.isOwner))

          for {
            demoted <- boards.demotePlayer(boardId, playerId)
            (players, users) <- boards.getPlayersWithUsers(boardId)

          } broadcast(boardId, SetPlayers(players, users))
      
      case DeleteGame(boardId) => {

        if (player.exists(_.isOwner)) {

          boards.deleteBoard(boardId)
          broadcast(boardId, SetBoard(None))
        }
      }

      case StartGame(boardId) => {

        if (player.exists(_.isOwner))

          for {
            started <- boards.startGame(boardId)
            board <- boards.getBoard(boardId)

          } if (started) broadcast(boardId, SetBoard(board))
      }

      case TakeAction(boardId, actionId) => {

        player foreach { player =>

          if (board.exists(_.ongoing)) {

            boards.takeAction(boardId, actionId, player.turnOrder)
            val action = ActionId(actionId, player.turnOrder)
            broadcast(boardId, PushActions(Seq(action)))
          }
        }
      }
    }
  }

  private def broadcast(boardId: String, res: BoardResponse) = {
    actors.get(boardId).getOrElse(Nil).foreach(_ ! res)
  }
}

object BoardManager {

  def props(db: Database)(implicit ec: ExecutionContext) =
    Props(new BoardManager(db))
}