package models.schema

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape
import models.schema.BoardSchema._
import models.schema.UserSchema._
import models.Player

object PlayerSchema {
  
  lazy val Players = TableQuery[Players]

  class Players(tag: Tag) extends Table[Player](tag, "Players") {

    val id = column[Int]("Id", O.PrimaryKey, O.AutoInc)
    val userId = column[Int]("UserId")
    val boardId = column[String]("BoardId")
    val turnOrder = column[Int]("TurnOrder")
    val isOwner = column[Boolean]("IsOwner")
    val time = column[Int]("Time")
    val resign = column[Boolean]("Resign")
    val draw = column[Boolean]("Draw")
    val revert = column[Option[Int]]("Revert")

    val userFk = (foreignKey("UserFk", userId, Users)
      (_.id, onDelete=ForeignKeyAction.SetNull))

    val boardFk = (foreignKey("BoardFk", boardId, Boards)
      (_.id, onDelete=ForeignKeyAction.Cascade))

    override val * : ProvenShape[Player] = {
      (id, userId, boardId, turnOrder, isOwner, time, resign,
        draw, revert) <> ((Player.apply _).tupled, Player.unapply)
    }
  }
}