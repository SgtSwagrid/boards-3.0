package models

import scala.math._
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.{ExecutionContext, Future}
import models.schema.BoardSchema._
import models.schema.PlayerSchema._

class BoardModel(db: Database)(implicit ec: ExecutionContext) {
  
  def createBoard(gameId: Int, userId: Int): Future[BoardRow] = {

    val boardId = (random() * (1 << 20)).toInt.toHexString.toUpperCase
    val board = BoardRow(id=boardId, gameId=gameId)
    val player = PlayerRow(userId=Some(userId),
      boardId=boardId, turnOrder=0, isOwner=true)

    for(
      _ <- db.run(Boards += board);
      _ <- db.run(Players += player)
    ) yield board
  }

  def boardById(boardId: String): Future[Option[BoardRow]] = {
    db.run(Boards.filter(_.id === boardId).result.headOption)
  }

  def playersByBoard(boardId: String): Future[Seq[PlayerRow]] = {
    db.run(Players.filter(_.boardId === boardId).result)
  }

  def playerByUser(boardId: String, userId: Int): Future[Option[PlayerRow]] = {
    db.run(Players
      .filter(_.boardId === boardId)
      .filter(_.userId === userId)
      .result.headOption)
  }
}