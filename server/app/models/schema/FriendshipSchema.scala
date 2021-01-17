package models.schema

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape
import java.time.LocalDateTime
import UserSchema.Users

object FriendshipSchema {
  
  lazy val Friendships = TableQuery[Friendships]

  case class Friendship(id: Int, user1Id: Int, user2Id: Int, date: LocalDateTime)

  class Friendships(tag: Tag) extends Table[Friendship](tag, "Friendships") {

    val id = column[Int]("Id", O.PrimaryKey, O.AutoInc)
    val user1Id = column[Int]("User1Id")
    val user2Id = column[Int]("User2Id")
    val status = column[Int]("Status")
    val date = column[LocalDateTime]("Date")

    val user1Fk = (foreignKey("User1Fk", user1Id, Users)
      (_.id, onDelete=ForeignKeyAction.Cascade))

    val user2Fk = (foreignKey("User2Fk", user2Id, Users)
      (_.id, onDelete=ForeignKeyAction.Cascade))

    override val * : ProvenShape[Friendship] = {
      (id, user1Id, user2Id, date) <> (Friendship.tupled, Friendship.unapply)
    }
  }
}