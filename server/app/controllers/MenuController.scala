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
    (protected implicit val ec: ExecutionContext) extends AbstractController(cc)
    with HasDatabaseConfigProvider[JdbcProfile] with UserRequest {

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
    withUser { user =>
      Future.successful(Ok(views.html.menus.create(user)))
    }
  }
}