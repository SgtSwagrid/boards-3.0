package models.schema

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape
import java.sql.Blob

object StateSchema {
  
  lazy val States = TableQuery[States]

  case class StateRow (id: Int, state: Blob)

  class States(tag: Tag) extends Table[StateRow](tag, "States") {

    val id = column[Int]("Id", O.PrimaryKey, O.AutoInc)
    val state = column[Blob]("State")

    override val * : ProvenShape[StateRow] = {
      (id, state) <> (StateRow.tupled, StateRow.unapply)
    }
  }
}