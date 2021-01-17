package controllers

import javax.inject._
import play.api.mvc._
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.ExecutionContext
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scala.concurrent.{ExecutionContext, Future}
import models.schema.UserSchema._
import models.{UserModel, BoardModel, SearchModel}
import controllers.helpers.{UserHelper, JsonHelper}
import protocols.SearchProtocol._
import io.circe.generic.auto._, io.circe.syntax._

@Singleton
class MenuController @Inject()
    (protected val dbConfigProvider: DatabaseConfigProvider, cc: ControllerComponents)
    (protected implicit val ec: ExecutionContext) extends AbstractController(cc)
    with HasDatabaseConfigProvider[JdbcProfile] with UserHelper with JsonHelper {

  private val users = new UserModel(db)
  private val boards = new BoardModel(db)
  private val search = new SearchModel(db)
  
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

  def browseQuery() = Action.async { implicit request =>
    withUserOpt { user =>
      withJson[SearchQuery] { query =>
        search.paginate(boards.boardQuery(), query)
          .map(r => Ok(r.asJson.toString))
      }
    }
  }

  def create() = Action.async { implicit request =>
    withUser { user =>
      Future.successful(Ok(views.html.menus.create(user)))
    }
  }
}