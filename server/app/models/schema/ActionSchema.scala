package models.schema

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape
import models.schema.BoardSchema._

object ActionSchema {
  
  lazy val Actions = TableQuery[Actions]

  case class Action (
    id: Int,
    boardId: String,
    action: Int,
    actionOrder: Int,
    turnOrder: Int
  )

  class Actions(tag: Tag) extends Table[Action](tag, "Actions") {

    val id = column[Int]("Id", O.PrimaryKey, O.AutoInc)
    val boardId = column[String]("BoardId")
    val action = column[Int]("Action")
    val actionOrder = column[Int]("ActionOrder")
    val turnOrder = column[Int]("TurnOrder")

    //val boardFk = (foreignKey("BoardFk", boardId, Boards)
    //  (_.id, onDelete=ForeignKeyAction.Cascade))

    override def * : ProvenShape[Action] = {
      (id, boardId, action, actionOrder, turnOrder) <>
        (Action.tupled, Action.unapply)
    }
  }
}