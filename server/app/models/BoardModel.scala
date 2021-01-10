package models

import scala.math._
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.{ExecutionContext, Future}
import models.schema.BoardSchema._
import models.schema.PlayerSchema._
import models.schema.ActionSchema._
import models.schema.UserSchema._
import models.{Board, Player, User}
import games.core.Manifest.Games
import slick.dbio.DBIOAction

class BoardModel(db: Database)(implicit ec: ExecutionContext) {

  private val userModel = new UserModel(db)
  
  def createBoard(gameId: Int, userId: Int): Future[Board] = {

    val boardId = randomId()
    val board = Board(id=boardId, gameId=gameId)
    val player = Player(userId=userId, boardId=boardId, turnOrder=0, isOwner=true)

    for {
      _ <- db.run(Boards += board) 
      _ <- db.run(Players += player)
      Some(board) <- db.run(boardById(boardId))
    } yield board
  }

  def joinBoard(boardId: String, userId: Int): Future[Option[Player]] = {

    db.run(boardWithPlayers(boardId)) flatMap {

      case Some((board, players))
        if canJoin(board, players, userId) => {

        val player = Player(userId=userId,
          boardId=boardId, turnOrder=players.size)

        for {
          _ <- db.run(Players += player)
          player <- db.run(playerByUser(boardId, userId))
        } yield player
      }
      case _ => Future.successful(None)
    }
  }

  def leaveBoard(boardId: String, userId: Int): Future[Boolean] = {

    for {
      // Find the player associated with this user.
      player <- db.run(playerByUser(boardId, userId))
      playerId <- Future.successful(player map(_.id) getOrElse(-1))

      // Leave the game.
      result <- db.run(Players.filter(_.id === playerId).delete).map(_ > 0)

      // Get the remaining players.
      players <- db.run(playersByBoard(boardId))

      // Delete the board if all players leave.
      _ <- if (players.size == 0)
          db.run(Boards.filter(_.id === boardId).delete)

      // Promote another player if the owner leaves.
        else if (player.map(_.isOwner).getOrElse(false))
          db.run(Players.filter(_.id === players.head.id)
            .map(_.isOwner).update(true))

        else Future.unit
    } yield result
  }

  def getBoard(boardId: String): Future[Option[Board]] = {
    db.run(boardById(boardId))
  }

  def boardExists(boardId: String): Future[Boolean] = {
    getBoard(boardId).map(_.isDefined)
  }

  def takeAction(boardId: String, action: Int): Future[Unit] = {

    db.run(for {
      actions <- actionsByBoard(boardId)
      _ <- Actions += ActionRow(-1, boardId, action, actions.size)
    } yield ())
  }

  def getPlayer(playerId: Int): Future[Option[Player]] = {
    db.run(playerById(playerId))
  }

  def getPlayers(boardId: String): Future[Seq[Player]] = {
    db.run(playersByBoard(boardId))
  }

  def getParticipants(boardId: String): Future[Seq[Participant]] = {
    db.run((Players.filter(_.boardId === boardId)
      join Users on (_.userId === _.id)).result)
      .map { _.map { case (player, user) => Participant(player, user) }}
  }

  private def boardById(boardId: String) = {
    Boards.filter(_.id === boardId).result.headOption
  }

  private def playersByBoard(boardId: String) = {
    Players.filter(_.boardId === boardId).result
  }

  private def boardWithPlayers(boardId: String): DBIO[Option[(Board, Seq[Player])]] = {
    for {
      board <- boardById(boardId)
      players <- playersByBoard(boardId)
    } yield board.map((_, players))
  }

  private def playersWithUsers(boardId: String) = {
    val players = Players.filter(_.boardId === boardId)
    (players join Users on (_.userId === _.id)).result
  }

  private def playerById(playerId: Int) = {
    Players.filter(_.id === playerId).result.headOption
  }

  private def playerWithUser(playerId: Int) = {
    playerById(playerId) flatMap {
      case Some(player) =>
        userModel.userById(player.userId)
          .map { _ map { user => Some((player, user)) }}
      case None => DBIOAction.successful(None)
    }
  }

  private def actionsByBoard(boardId: String) = {
    Actions.filter(_.boardId === boardId).sortBy(_.actionOrder).result
  }

  private def playerByUser(boardId: String, userId: Int) = {
    Players
      .filter(_.boardId === boardId)
      .filter(_.userId === userId)
      .result.headOption
  }

  private def randomId() = {
    (random() * (1 << 20)).toInt.toHexString
      .toUpperCase.padTo(5, "0").reverse.toString
  }

  private def canJoin(board: Board, players: Seq[Player], userId: Int) = {
    players.size < Games(board.gameId).players.max &&
      !players.exists(_.userId == userId) &&
      board.status == 0
  }
}