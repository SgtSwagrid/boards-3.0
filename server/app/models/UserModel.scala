package models

import slick.jdbc.MySQLProfile.api._
import scala.concurrent.{ExecutionContext, Future}
import org.mindrot.jbcrypt.BCrypt
import models.schema.UserSchema._
import forms.UserForms._

class UserModel(db: Database)(implicit ec: ExecutionContext) {

  def getUser(userId: Int): Future[Option[User]] = {
    db.run(userById(userId))
  }
  
  def createUser(registration: Registration):
      Future[Either[InvalidUser, User]] = {

    registration match {

      case Left(error) => Future.successful(Left(error))
      case Right(RegisterForm(Username(username), Password(password))) => {

       db.run(userByName(username)) flatMap {

          case Some(_) => Future.successful(Left(UsernameTaken))
          case None => {

            val hash = BCrypt.hashpw(password, BCrypt.gensalt())
            db.run(Users += User(-1, username, hash))
              .flatMap(_ => db.run(userByName(username))
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

  def userById(userId: Int) = {
    Users.filter(_.id === userId).result.headOption
  }

  def userByName(username: String) = {
    Users.filter(_.username === username).result.headOption
  }
}