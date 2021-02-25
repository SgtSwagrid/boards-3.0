package models.schema

//import java.time.LocalDateTime
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
    val rematchBaseId = column[Option[String]]("RematchBaseId")
    val forkBaseId = column[Option[String]]("ForkBaseId")
    val forkPly = column[Option[Int]]("ForkPly")
    val modified = column[String]("Modified")

    /*val rematchBoardFk = (foreignKey("RematchBoardFk", rematchBoardId, Boards)
      (_.id.?, onDelete=ForeignKeyAction.SetNull))

    val parentBoardFk = (foreignKey("ParentBoardFk", parentBoardId, Boards)
      (_.id.?, onDelete=ForeignKeyAction.SetNull))*/

    override def * : ProvenShape[Board] = {
      (id, gameId, isPublic, status, rematchBaseId, forkBaseId, forkPly, modified) <>
        (Board.tupled, Board.unapply)
    }
  }
}