package models.schema

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape
import models.schema.BoardSchema._
import models.schema.UserSchema._

object PlayerSchema {
  
  lazy val Players = TableQuery[Players]

  case class PlayerRow(
    id: Int = -1,
    userId: Option[Int],
    boardId: String,
    turnOrder: Int,
    isOwner: Boolean = false,
    time: Int = 0,
    resignOffer: Boolean = false,
    drawOffer: Boolean = false,
    undoOffer: Boolean = false
  )

  class Players(tag: Tag) extends Table[PlayerRow](tag, "Players") {

    val id = column[Int]("Id", O.PrimaryKey, O.AutoInc)
    val userId = column[Option[Int]]("UserId")
    val boardId = column[String]("BoardId")
    val turnOrder = column[Int]("TurnOrder")
    val isOwner = column[Boolean]("IsOwner")
    val time = column[Int]("Time")
    val resignOffer = column[Boolean]("ResignOffer")
    val drawOffer = column[Boolean]("DrawOffer")
    val undoOffer = column[Boolean]("UndoOffer")

    val userFk = (foreignKey("UserFk", userId, Users)
      (_.id.?, onDelete=ForeignKeyAction.SetNull))

    val boardFk = (foreignKey("BoardFk", boardId, Boards)
      (_.id, onDelete=ForeignKeyAction.Cascade))

    override val * : ProvenShape[PlayerRow] = {
      (id, userId, boardId, turnOrder, isOwner, time, resignOffer,
        drawOffer, undoOffer) <> (PlayerRow.tupled, PlayerRow.unapply)
    }
  }
}
