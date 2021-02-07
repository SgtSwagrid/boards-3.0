package models

import slick.jdbc.MySQLProfile.api._
import scala.concurrent.{ExecutionContext, Future}
import org.mindrot.jbcrypt.BCrypt
import models.schema.UserSchema._
import forms.UserForms._
import models.protocols.SearchProtocol.SearchQuery
import models.protocols.UserProtocol.UserFilter
import models.protocols.SearchProtocol.SearchResponse
import models.protocols.UserProtocol._
import models.Friend
import io.circe.Decoder.state
import models.schema.FriendSchema.Friends
import akka.http.javadsl.model.DateTime
import java.time.LocalDateTime

class UserModel(db: Database)(implicit ec: ExecutionContext) {

  // Query[TABLE, OUTPUT TYPE, OUTPUT STRUCTURE]
  type UsersQuery = Query[Users, User, Seq]

  private val search = new SearchModel(db)

  def getUser(userId: Int): Future[Option[User]] = {
    db.run(DBAction.getUserById(userId))
  }
  
  def createUser(registration: Registration):
      Future[Either[InvalidUser, User]] = {

    registration match {

      case Left(error) => Future.successful(Left(error))
      case Right(RegisterForm(Username(username), Password(password))) => {

       db.run(DBAction.getUserByName(username)) flatMap {

          case Some(_) => Future.successful(Left(UsernameTaken))
          case None => {

            val hash = BCrypt.hashpw(password, BCrypt.gensalt())
            db.run(Users += User(-1, username, hash))
              .flatMap(_ => db.run(DBAction.getUserByName(username))
                .map(user => Right(user.get)))
          }
        }
      }
    }
  }

  def validateUser(login: Login): Future[Either[InvalidUser, User]] = {

    login match {

      case Left(error) => Future.successful(Left(error))
      case Right(LoginForm(Username(username), Password(password))) => {

        db.run(Users.filter(_.username === username).result.headOption) map {

          case None => Left(UnknownUsername)
          case Some(user) => {

            println(BCrypt.checkpw(password, user.password))

            Right(user).filterOrElse(user =>
              BCrypt.checkpw(password, user.password),
              IncorrectPassword)
          }
        }
      }
    }
  }

  def searchUsers(query: SearchQuery[UserFilter]): Future[SearchResponse[User]] = {

    val usersResults = query.filters.foldLeft[UsersQuery] (Users) {
      // query q, filters f 
      (q, f) => f match { 

        case NameContainsSubstring(substring: String) => 
          q.filter(_.username.toUpperCase like s"%$substring%".toUpperCase())

        case NameAlphabetical => q.sortBy(_.username)
          
        case PendingFriends(userId: Int) => q.filter { user => 
          val friends = DBQuery.pendingFriendsByReceiver(userId)
          friends.filter { friend => friend.user1Id === user.id }.exists
        }

        case AcceptedFriends(userId: Int) => q.filter { user => 
          val friends = DBQuery.friendsByUser(userId)
          friends.filter { friend => friend.id === user.id }.exists
        }

      }
    }

    search.paginate(usersResults, query)
  }

  def getUserByName(username: String): Future[Option[User]] = {
    db.run(DBAction.getUserByName(username))
  }

  private[models] object DBQuery {

    def userById(userId: Int) =
      Users.filter(_.id === userId)

    def userByName(username: String) =
      Users.filter(_.username === username)

    // todo I think the order of these filters should be reversed
    def friendsByUserByStatus(userId: Int, friendStatus: Int) = 
      Users.filter { user =>
        Friends.filter { friend =>
          ((friend.user1Id === userId && friend.user2Id === user.id) ||
          (friend.user2Id === userId && friend.user1Id === user.id)) && 
          (friend.status === friendStatus)
        }.exists
      }

    def friendsByReceiverByStatus(receiverId: Int, friendStatus: Int) = 
      Friends.filter { friend =>
        Users.filter { user =>
          (friend.user1Id === user.id && friend.user2Id === receiverId) && 
          (friend.status === friendStatus)
        }.exists
      }

    def friendsBySenderByStatus(senderId: Int, friendStatus: Int) = 
      Friends.filter { friend =>
        Users.filter { user =>
          (friend.user1Id === senderId && friend.user2Id === user.id) && 
          (friend.status === friendStatus)
        }.exists
      }

    def pendingFriendsByUser(userId: Int) =
      friendsByUserByStatus(userId, 0)

    def friendsByUser(userId: Int) =
      friendsByUserByStatus(userId, 1)

    def declinedFriendsByUser(userId: Int) =
      friendsByUserByStatus(userId, 2)

    def friendById(friendId: Int) = 
      Friends.filter(_.id === friendId)

    def pendingFriendsByReceiver(userId: Int) =
      friendsByReceiverByStatus(userId, 0)

    def declinedFriendsByReceiver(userId: Int) =
      friendsByReceiverByStatus(userId, 2)

  }


  private[models] object DBAction {

    def getUserById(userId: Int) = 
      DBQuery.userById(userId).result.headOption

    def getUserByName(username: String) =
      DBQuery.userByName(username).result.headOption

    def getFriendsByUser(userId: Int) =
      DBQuery.friendsByUser(userId).result

    def getPendingFriendsByUser(userId: Int) =
      DBQuery.pendingFriendsByUser(userId).result

    //def getPendingFriendsByReceiver(userId: Int) =
    //  DBQuery.friendsByReceiverByStatus(userId, 0)

    //def getDeclinedFriendsByReceiver(userId: Int) =
    //  DBQuery.friendsByReceiverByStatus(userId, 2)

    def acceptFriend(senderId: Int, receiverId: Int) =
      DBQuery.friendsByReceiverByStatus(receiverId, 0)
        .filter(_.user1Id === senderId)
        .map(_.status)
        .update(1)

    def declineFriend(senderId: Int, receiverId: Int) =
      DBQuery.friendsByReceiverByStatus(receiverId, 0)
        .filter(_.user1Id === senderId)
        .map(_.status)
        .update(2)
  }

  def createFriend(senderId: Int, receiverId: Int): Future[Friend] = {

    val friend = Friend(user1Id=senderId, user2Id=receiverId, 
      status=0, date=LocalDateTime.now())
    
    db.run(Friends += friend).map(_ => friend)
  }

  def acceptFriend(senderId: Int, receiverId: Int): Future[Boolean] = {
    db.run(DBAction.acceptFriend(senderId, receiverId)).map(_ > 0)
  }

  def declineFriend(senderId: Int, receiverId: Int): Future[Boolean] = {
    db.run(DBAction.declineFriend(senderId, receiverId)).map(_ > 0)
  }

  def friendsOfUser(userId: Int) = {
    db.run(DBAction.getFriendsByUser(userId))
  }
  
  def friendRequestsOfuser(userId: Int) = {
    db.run(DBAction.getPendingFriendsByUser(userId))
  }

}