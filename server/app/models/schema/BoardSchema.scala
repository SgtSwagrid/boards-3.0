package models.schema

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape
import play.api.libs.json._
import models.schema.StateSchema._

object BoardSchema {
  
  lazy val Boards = TableQuery[Boards]

  case class BoardRow (
    id: String,
    gameId: Int,
    stateId: Option[Int] = None,
    isPublic: Boolean = true,
    status: Int = 0,
    rematchBoardId: Option[String] = None,
    parentBoardId: Option[String] = None,
    parentStateId: Option[Int] = None
  )

  class Boards(tag: Tag) extends Table[BoardRow](tag, "Boards") {

    val id = column[String]("Id", O.PrimaryKey)
    val gameId = column[Int]("GameId")
    val stateId = column[Option[Int]]("StateId")
    val isPublic = column[Boolean]("IsPublic")
    val status = column[Int]("Status")
    val rematchBoardId = column[Option[String]]("RematchBoardId")
    val parentBoardId = column[Option[String]]("ParentBoardId")
    val parentStateId = column[Option[Int]]("ParentStateId")

    /*val stateFk = (foreignKey("StateFk", stateId, States)
      (_.id.?, onDelete=ForeignKeyAction.Restrict))

    val rematchBoardFk = (foreignKey("RematchBoardFk", rematchBoardId, Boards)
      (_.id.?, onDelete=ForeignKeyAction.SetNull))

    val parentBoardFk = (foreignKey("ParentBoardFk", parentBoardId, Boards)
      (_.id.?, onDelete=ForeignKeyAction.SetNull))

    val parentStateFk = (foreignKey("ParentStateFk", parentStateId, States)
      (_.id.?, onDelete=ForeignKeyAction.SetNull))*/

    override def * : ProvenShape[BoardRow] = {
      (id, gameId, stateId, isPublic, status, rematchBoardId,
        parentBoardId, parentStateId) <> (BoardRow.tupled, BoardRow.unapply)
    }
  }
}
