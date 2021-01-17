package controllers

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc.{AbstractController, ControllerComponents, Request, Result, AnyContent}
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import models.schema.UserSchema._
import models.UserModel
import forms.UserForms._
import controllers.helpers.UserHelper

@Singleton
class UserController @Inject()
    (protected val dbConfigProvider: DatabaseConfigProvider, cc: ControllerComponents)
    (protected implicit val ec: ExecutionContext) extends AbstractController(cc)
    with HasDatabaseConfigProvider[JdbcProfile] with UserHelper {
  
  private val userModel = new UserModel(db)

  def login(next: String) = Action.async { implicit request =>
    withUserOpt { user =>

      request.body.asFormUrlEncoded map { form =>
        userModel.validateUser(LoginForm(form)) map {
          _.map(user => Redirect(next)
            .withSession(
              "userId" -> user.id.toString,
              "csrfToken" -> play.filters.csrf.CSRF.getToken.map(_.value).getOrElse("")))
          .left.map(error => Redirect(routes.UserController.login())
            .flashing(showError(error))).merge
        }
      } getOrElse(Future.successful(Ok(views.html.users.login(user))))
    }
  }

  def register(next: String) = Action.async { implicit request =>
    withUserOpt { user =>
      
      request.body.asFormUrlEncoded map { form =>
        userModel.createUser(RegisterForm(form)) map {
          _.map(user => Redirect(next)
            .withSession(
              "userId" -> user.id.toString,
              "csrfToken" -> play.filters.csrf.CSRF
                .getToken.map(_.value).getOrElse("")))
          .left.map(error => Redirect(routes.UserController.register())
            .flashing(showError(error))).merge
        }
      } getOrElse(Future.successful(Ok(views.html.users.register(user))))
    }
  }

  def logout(next: String) = Action { implicit request =>
    Redirect(next).withNewSession
  }

  def userProfile() = Action.async { implicit request =>
    withUser { user =>
      Future.successful(Ok(views.html.users.profile(user)))
    }
  }

  def userFriends() = Action.async { implicit request =>
    withUser { user =>
      Future.successful(Ok(views.html.users.friends(user)))
    }
  }

  private def showError(error: InvalidUser): (String, String) = "error" -> (error match {

    case UnknownUsername => "User does not exist."
    case UsernameTaken => "Username is already taken."
    case UsernameTooShort => s"Username must be at least $MinUsernameSize characters."
    case UsernameTooLong => s"Username must be at most $MaxUsernameSize characters."
    case IllegalUsernameChar => "Username contains an invalid character."
    case NoUsername => "No username was given."
    case IncorrectPassword => "Password is not correct."
    case PasswordTooShort => s"Password must be at least $MinPasswordSize characters."
    case PasswordTooLong => s"Password must be at most $MaxPasswordSize characters."
    case IllegalPasswordChar => "Password contain an invalid character."
    case NoPassword => "No password was given."
  })
}