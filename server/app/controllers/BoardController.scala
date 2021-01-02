package controllers

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc.{AbstractController, ControllerComponents, Request, Result, AnyContent}
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json._
import models.schema.BoardSchema._
import models.schema.UserSchema._
import models.BoardModel

@Singleton
class BoardController @Inject()
    (protected val dbConfigProvider: DatabaseConfigProvider, cc: ControllerComponents)
    (protected implicit val ec: ExecutionContext) extends AbstractController(cc)
    with HasDatabaseConfigProvider[JdbcProfile] with UserRequest with JsonRequest {

  private val boardModel = new BoardModel(db)
  
  def create(gameId: Int) = Action.async { implicit request =>
    withUser { user =>
      boardModel.createBoard(gameId, user.id) map { boardId =>
        Ok(Json.toJson(boardId))
      }
    }
  }

  def game(boardId: String) = Action.async { implicit request =>
    withUser { user =>
      getOr404(boardModel.getBoard(boardId)) { board =>
        Future.successful(Ok(views.html.games.board(user, board.id)))
      }
    }
  }

  def details(boardId: String) = Action.async { implicit request =>
    withUser { user => 
      getOr404(boardModel.getBoard(boardId)) { board =>
        Future.successful(Ok(Json.toJson(board)))
      }
    }
  }

  def join(boardId: String) = Action.async { implicit request =>
    withUser { user =>
      boardModel.joinBoard(boardId, user.id)
        .map(s => Ok(Json.toJson(s)))
    }
  }

  def leave(boardId: String) = Action.async { implicit request =>
    withUser { user =>
      boardModel.leaveBoard(boardId, user.id)
        .map(s => Ok(Json.toJson(s)))
    }
  }
}