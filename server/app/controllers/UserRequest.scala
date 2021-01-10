package controllers

import scala.concurrent.{ExecutionContext, Future}
import javax.inject._
import play.api.mvc.{AbstractController, Request, Result, AnyContent}
import slick.jdbc.JdbcProfile
import play.api.db.slick.HasDatabaseConfigProvider
import models.{User, UserModel}

trait UserRequest {
    this: AbstractController with HasDatabaseConfigProvider[JdbcProfile] =>

  protected implicit val ec: ExecutionContext
  private val userModel = new UserModel(db)
  
  protected def withUser(f: User => Future[Result])
      (implicit request: Request[AnyContent]): Future[Result] = {
    
    request.session.get("userId") match {
      case Some(userId) => userModel.getUser(userId.toInt) flatMap {
        case Some(user) => f(user)
        case None => Future.successful(
          Redirect(routes.UserController.login(request.path)))
      }
      case None => Future.successful(
        Redirect(routes.UserController.login(request.path)))
    }
  }

  protected def withUserOpt(f: Option[User] => Future[Result])
      (implicit request: Request[AnyContent]): Future[Result] = {
    
    request.session.get("userId") match {
      case Some(userId) => userModel.getUser(userId.toInt).flatMap(f)
      case None => f(None)
    }
  }

  protected def getOr404[T](o: Future[Option[T]])(f: T => Future[Result])
      (implicit request: Request[AnyContent]): Future[Result] = {
    
    o flatMap {
      case Some(value) => f(value)
      case None => withUserOpt { user =>
        Future.successful(NotFound(views.html.menus.notfound(user)))
      }
    }
  }
}