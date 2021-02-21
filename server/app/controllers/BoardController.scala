package controllers

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc._, play.api.libs.streams.ActorFlow
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile, slick.jdbc.MySQLProfile.api._
import akka.actor.{Actor, ActorRef, Props, ActorSystem}, akka.stream.Materializer
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import models.{BoardModel, UserModel, Board, Player, User}
import actors.{BoardActor, BoardManager}
import controllers.helpers.{UserHelper, ResourceHelper}
import games.Chess

@Singleton
class BoardController @Inject()
    (protected val dbConfigProvider: DatabaseConfigProvider, cc: ControllerComponents)
    (implicit protected val ec: ExecutionContext, system: ActorSystem, mat: Materializer)
    extends AbstractController(cc) with HasDatabaseConfigProvider[JdbcProfile]
    with UserHelper with ResourceHelper {

  private val boards = new BoardModel(db)
  private val users = new UserModel(db)

  private val manager = system.actorOf(BoardManager.props(db))
  
  def create(gameId: Int) = Action.async { implicit request =>
    withUser { user =>
      boards.createBoard(gameId, user.id) map { board =>
        Ok(board.asJson.toString)
      }
    }
  }

  def socket(boardId: String) = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef { out =>
      val userId = request.session("userId").toInt
      BoardActor.props(out, manager, boardId, userId)
    }
  }

  def game(boardId: String) = Action.async { implicit request =>
    withUser { user =>
      getOr404(boards.getBoard(boardId)) { board =>
        Future.successful(Ok(views.html.games.board(user, board.id)))
      }
    }
  }
}