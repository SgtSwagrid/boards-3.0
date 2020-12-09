package models.schema

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape

object UserSchema {
  
  lazy val Users = TableQuery[Users]

  case class User(id: Int, username: String, password: String)

  class Users(tag: Tag) extends Table[User](tag, "Users") {

    val id = column[Int]("Id", O.PrimaryKey, O.AutoInc)
    val username = column[String]("Username")
    val password = column[String]("Password")

    override def * : ProvenShape[User] = {
      (id, username, password) <> (User.tupled, User.unapply)
    }
  }
}