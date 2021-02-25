package actors

import scala.concurrent.ExecutionContext
import scala.collection.mutable
import slick.jdbc.MySQLProfile.api.Database
import akka.actor.{Actor, ActorRef, Props}
import models.{BoardModel, UserModel}
import models.{Board, Player}
import models.protocols.BoardProtocol._
import scala.concurrent.Future

class BoardManager(boardId: String, db: Database)
    (implicit ec: ExecutionContext) extends Actor {

  private val boards = new BoardModel(db)
  private val users = new UserModel(db)

  private val spectators = mutable.Set[ActorRef]()

  def receive = { case (actor: ActorRef, thisUserId: Int, req: BoardRequest) =>
    
    for {
      Some(board) <- boards.getBoard(boardId)
      thisPlayer <- boards.getPlayerByUser(boardId, thisUserId)
    } req match {

      case NewSpectator =>
        newSpectator(actor)

      case AddPlayer(targetUserId) =>
        addPlayer(thisUserId, targetUserId)
      
      case RemovePlayer(targetPlayerId) =>
        removePlayer(thisPlayer, targetPlayerId)

      case PromotePlayer(targetPlayerId) =>
        promotePlayer(thisPlayer, targetPlayerId)

      case DemotePlayer(targetPlayerId) =>
        demotePlayer(thisPlayer, targetPlayerId)

      case StartGame =>
        startGame(thisPlayer)
      
      case DeleteGame =>
        deleteGame(thisPlayer)

      case ResignGame =>
        resignGame(thisPlayer)

      case DrawGame =>
        drawGame(thisPlayer)

      case RematchGame =>
        rematchGame(actor, thisPlayer)

      case ForkGame(ply) =>
        forkGame(actor, thisPlayer, ply)

      case TakeAction(actionId) =>
        takeAction(board, thisPlayer, actionId)
    }
  }

  private def updateSession(actor: ActorRef) = {

    for {
      board <- boards.getBoard(boardId)
      players <- boards.getPlayers(boardId)
      users <- boards.getUsers(boardId)
      rematch <- boards.getRematch(boardId)
      forks <- boards.getForks(boardId)

    } yield board match {
      case Some(board) => {
        val cleanedUsers = users.map(_.copy(password = ""))
        actor ! UpdateSession(board, players, cleanedUsers, rematch, forks)
      }
      case None => actor ! Redirect(None)
    } 
  }

  private def newSpectator(actor: ActorRef) = {

    spectators += actor

    for {
      _ <- updateSession(actor)
      actions <- boards.getActions(boardId)
      
    } actor ! PushActions(actions.map { action =>
      ActionLog(action.action, action.turnOrder)
    })
  }

  private def addPlayer(thisUserId: Int, targetUserId: Int) = {

    if (thisUserId == targetUserId) {

      for (_ <- boards.joinBoard(boardId, targetUserId))
        spectators.foreach(updateSession)
    }
  }

  private def removePlayer(thisPlayer: Option[Player], targetPlayerId: Int) = {

    if (thisPlayer.exists(p => p.isOwner || p.id == targetPlayerId)) {

      for (_ <- boards.removePlayer(boardId, targetPlayerId))
        spectators.foreach(updateSession)
    }
  }

  private def promotePlayer(thisPlayer: Option[Player], targetPlayerId: Int) = {

    if (thisPlayer.exists(_.isOwner)) {

      for (_ <- boards.promotePlayer(boardId, targetPlayerId))
        spectators.foreach(updateSession)
    }
  }

  private def demotePlayer(thisPlayer: Option[Player], targetPlayerId: Int) = {

    if (thisPlayer.exists(_.isOwner)) {

      for (_ <- boards.demotePlayer(boardId, targetPlayerId))
        spectators.foreach(updateSession)
    }
  }

  private def startGame(thisPlayer: Option[Player]) = {

    if (thisPlayer.exists(_.isOwner)) {

      for (_ <- boards.startGame(boardId))
        spectators.foreach(updateSession)
    }
  }

  private def deleteGame(thisPlayer: Option[Player]) = {

    if (thisPlayer.exists(_.isOwner)) {

      for (_ <- boards.deleteBoard(boardId))
        spectators.foreach(updateSession)
    }
  }

  private def resignGame(thisPlayer: Option[Player]) = {

    thisPlayer.map { thisPlayer =>

      for (_ <- boards.resign(boardId, thisPlayer.id))
        spectators.foreach(updateSession)
    }
  }

  private def drawGame(thisPlayer: Option[Player]) = {

    thisPlayer.map { thisPlayer =>

      for (_ <- boards.draw(boardId, thisPlayer.id))
        spectators.foreach(updateSession)
    }
  }

  private def rematchGame(actor: ActorRef, thisPlayer: Option[Player]) = {

    thisPlayer.map { thisPlayer =>

      for (rematch <- boards.rematch(boardId, thisPlayer.id)) {
        spectators.filter(_ != actor).foreach(updateSession)
        actor ! Redirect(Some(rematch))
      }
    }
  }

  private def forkGame(actor: ActorRef, thisPlayer: Option[Player], ply: Int) = {

    thisPlayer.map { thisPlayer =>
    
      for (fork <- boards.fork(boardId, thisPlayer.id, ply)) {
        spectators.filter(_ != actor).foreach(updateSession)
        actor ! Redirect(Some(fork))
      }
    }
  }

  private def takeAction(board: Board, thisPlayer: Option[Player], actionId: Int) = {

    thisPlayer.map { thisPlayer =>

      if (board.ongoing) {
      
        for {
          _ <- boards.takeAction(boardId, actionId, thisPlayer.turnOrder)
          _ <- Future.sequence(spectators.map(updateSession))
        } {
          val actionLog = ActionLog(actionId, thisPlayer.turnOrder)
          spectators.foreach(_ ! PushActions(Seq(actionLog)))
        }
      }
    }
  }
}

object BoardManager {

  def props(boardId: String, db: Database)(implicit ec: ExecutionContext) =
    Props(new BoardManager(boardId, db))
}