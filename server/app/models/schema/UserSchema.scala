package models.schema

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape
import models.User

object UserSchema {
  
  lazy val Users = TableQuery[Users]

  class Users(tag: Tag) extends Table[User](tag, "Users") {

    val id = column[Int]("Id", O.PrimaryKey, O.AutoInc)
    val username = column[String]("Username")
    val password = column[String]("Password")

    override def * : ProvenShape[User] = {
      (id, username, password) <> ((User.apply _).tupled, User.unapply)
    }
  }
}