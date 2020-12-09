package controllers

import javax.inject._
import play.api.mvc._
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.ExecutionContext
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scala.concurrent.{ExecutionContext, Future}
import models.schema.UserSchema._
import models.UserModel

@Singleton
class MenuController @Inject()
    (protected val dbConfigProvider: DatabaseConfigProvider, cc: ControllerComponents)
    (implicit ec: ExecutionContext)
    extends AbstractController(cc) with HasDatabaseConfigProvider[JdbcProfile] {

  private val userModel = new UserModel(db)
  
  def index() = Action.async { implicit request =>
    withUserOpt { user =>
      Future.successful(Ok(views.html.menus.index(user)))
    }
  }

  def browse() = Action.async { implicit request =>
    withUserOpt { user =>
      Future.successful(Ok(views.html.menus.browse(user)))
    }
  }

  def create() = Action.async { implicit request =>
    withUserOpt { user =>
      Future.successful(Ok(views.html.menus.create(user)))
    }
  }

  private def withUser(f: User => Future[Result])
      (implicit request: Request[AnyContent]): Future[Result] = {
    
    request.session.get("userId") match {
      case Some(userId) => userModel.userById(userId.toInt) flatMap {
        case Some(user) => f(user)
        case None => Future.successful(
          Redirect(routes.MenuController.index()))
      }
      case None => Future.successful(
        Redirect(routes.UserController.login(request.path)))
    }
  }

  private def withUserOpt(f: Option[User] => Future[Result])
      (implicit request: Request[AnyContent]): Future[Result] = {
    
    request.session.get("userId") match {
      case Some(userId) => userModel.userById(userId.toInt).flatMap(f)
      case None => f(None)
    }
  }
}