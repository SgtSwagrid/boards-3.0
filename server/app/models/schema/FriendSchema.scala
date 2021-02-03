package models.schema

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape
import java.time.LocalDateTime
import models.Friend
import UserSchema.Users

object FriendSchema {
  
  lazy val Friends = TableQuery[Friends]

  class Friends(tag: Tag) extends Table[Friend](tag, "Friends") {

    def id = column[Int]("Id", O.PrimaryKey, O.AutoInc)
    def user1Id = column[Int]("User1Id")
    def user2Id = column[Int]("User2Id")
    def status = column[Int]("Status")
    def date = column[LocalDateTime]("Date")

    def user1Fk = (foreignKey("User1Fk", user1Id, Users)
      (_.id, onDelete=ForeignKeyAction.Cascade))

    def user2Fk = (foreignKey("User2Fk", user2Id, Users)
      (_.id, onDelete=ForeignKeyAction.Cascade))

    override val * : ProvenShape[Friend] = {
      (id, user1Id, user2Id, status, date) <> (Friend.tupled, Friend.unapply)
    }
  }
}