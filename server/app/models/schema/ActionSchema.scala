package models.schema

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape
import models.schema.BoardSchema._

object ActionSchema {
  
  lazy val Actions = TableQuery[Actions]

  case class ActionRow (
    id: Int,
    boardId: String,
    action: Int,
    actionOrder: Int
  )

  class Actions(tag: Tag) extends Table[ActionRow](tag, "Actions") {

    val id = column[Int]("Id", O.PrimaryKey, O.AutoInc)
    val boardId = column[String]("BoardId")
    val action = column[Int]("Action")
    val actionOrder = column[Int]("ActionOrder")

    //val boardFk = (foreignKey("BoardFk", boardId, Boards)
    //  (_.id, onDelete=ForeignKeyAction.Cascade))

    override def * : ProvenShape[ActionRow] = {
      (id, boardId, action, actionOrder) <>
        (ActionRow.tupled, ActionRow.unapply)
    }
  }
}