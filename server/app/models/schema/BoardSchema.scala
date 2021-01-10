package models.schema

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape
import models.Board

object BoardSchema {
  
  lazy val Boards = TableQuery[Boards]

  class Boards(tag: Tag) extends Table[Board](tag, "Boards") {

    val id = column[String]("Id", O.PrimaryKey)
    val gameId = column[Int]("GameId")
    val isPublic = column[Boolean]("IsPublic")
    val status = column[Int]("Status")
    val rematchBoardId = column[Option[String]]("RematchBoardId")
    val parentBoardId = column[Option[String]]("ParentBoardId")

    /*val stateFk = (foreignKey("StateFk", stateId, States)
      (_.id.?, onDelete=ForeignKeyAction.Restrict))

    val rematchBoardFk = (foreignKey("RematchBoardFk", rematchBoardId, Boards)
      (_.id.?, onDelete=ForeignKeyAction.SetNull))

    val parentBoardFk = (foreignKey("ParentBoardFk", parentBoardId, Boards)
      (_.id.?, onDelete=ForeignKeyAction.SetNull))

    val parentStateFk = (foreignKey("ParentStateFk", parentStateId, States)
      (_.id.?, onDelete=ForeignKeyAction.SetNull))*/

    override def * : ProvenShape[Board] = {
      (id, gameId, isPublic, status, rematchBoardId, parentBoardId) <>
        ((Board.apply _).tupled, Board.unapply)
    }
  }
}
