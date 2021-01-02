package models

import scala.math._
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.{ExecutionContext, Future}
import models.schema.BoardSchema._
import models.schema.PlayerSchema._
import models.schema.ActionSchema._
import models.schema.UserSchema._
import games.core.GameInstance._
import games.core.Manifest.Games

class BoardModel(db: Database)(implicit ec: ExecutionContext) {

  private val userModel = new UserModel(db)
  
  def createBoard(gameId: Int, userId: Int): Future[String] = {

    val boardId = randomId()
    val board = BoardRow(id=boardId, gameId=gameId)
    val player = PlayerRow(userId=Some(userId),
      boardId=boardId, turnOrder=0, isOwner=true)

    for {
      _ <- db.run(Boards += board) 
      _ <- db.run(Players += player)
     } yield boardId
  }

  def getBoard(boardId: String): Future[Option[Board]] = {

    db.run(for {
      board <- boardById(boardId)
      players <- playersWithUsers(boardId)
      actions <- actionsByBoard(boardId)

    } yield board map { board => Board (
      board.id,
      board.gameId,
      board.isPublic,
      board.status,
      players map { case (player, user) => Player (
        player.id,
        player.userId,
        user.username,
        player.turnOrder,
        player.isOwner,
        player.time,
        player.resignOffer,
        player.drawOffer,
        player.undoOffer
      )},
      actions map(_.action),
      board.rematchBoardId,
      board.parentBoardId
    )})
  }

  def joinBoard(boardId: String, userId: Int): Future[Boolean] = {

    for {
      // Get the board, and list of current players.
      board <- db.run(boardById(boardId))
      players <- db.run(playersByBoard(boardId))
      
      result <- board
      
      // Ensure the game isn't already full.
        .filter(board => players.size < Games(board.gameId).players.max)

      // Ensure the user isn't already in this game.
        .filter(_ => !players.exists(_.userId == Some(userId)))

      // Ensure the game hasn't already started.
        .filter(_.status > 0)

      // Add the player to the game.
        .map(_ => db.run(Players +=
          PlayerRow(-1, Some(userId), boardId, players.size))
        .map(_ > 0)) getOrElse(Future.successful(false))

    } yield result
  }

  def leaveBoard(boardId: String, userId: Int): Future[Boolean] = {

    for {
      // Find the player associated with this user.
      player <- db.run(playerByUser(boardId, userId))
      playerId <- Future.successful(player map(_.id) getOrElse(-1))

      //Leave the game.
      result <- db.run(Players.filter(_.id === playerId).delete) map(_ > 0)

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

  def boardExists(boardId: String): Future[Boolean] = {
    db.run(boardById(boardId)) map(_.isDefined)
  }

  def takeAction(boardId: String, action: Int): Future[Unit] = {

    db.run(for {
      actions <- actionsByBoard(boardId)
      _ <- Actions += ActionRow(-1, boardId, action, actions.size)
    } yield ())
  }

  private def boardById(boardId: String) = {
    Boards.filter(_.id === boardId).result.headOption
  }

  private def playersByBoard(boardId: String) = {
    Players.filter(_.boardId === boardId).result
  }

  private def boardWithPlayers(boardId: String) = {
    db.run(for {
      board <- boardById(boardId)
      players <- playersByBoard(boardId)
    } yield board.map((_, players)))
  }

  private def playersWithUsers(boardId: String) = {
    val players = Players.filter(_.boardId === boardId)
    (players join Users on (_.userId === _.id)).result
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

  private def randomId() =
    (random() * (1 << 20)).toInt.toHexString
      .toUpperCase.padTo(5, "0").reverse.toString
}