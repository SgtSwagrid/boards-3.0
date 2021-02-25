package views.game

import org.scalajs.dom._

import slinky.core.{Component, StatelessComponent}
import slinky.core.facade.ReactElement
import slinky.core.annotations.react
import slinky.web.ReactDOM
import slinky.web.html._

import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

import models.{Board, Player, User}
import models.protocols.BoardProtocol._

import games.core.State.AnyState

@react class GameComponent extends Component {

  private def gameRoute(boardId: String) = s"/games/board/$boardId"

  case class Props (
    boardId: String,
    user: User,
    socket: WebSocket
  )

  case class State (
    session: Option[Session.AnySession] = None,
    gameState: Option[AnyState] = None
  )

  def initialState = State()

  def render() = state.session.map { session => div (

    SidebarComponent(session,
      s => setState(_.copy(session = Some(s)))),
    BoardComponent(session)
  )}


  override def componentDidMount() = {

    props.socket.onmessage = { (e: MessageEvent) =>
      
      decode[BoardResponse](e.data.toString).toOption.get match {

        case UpdateSession(board, players, users, rematch, forks) => {

          val gameState = {

            if (!state.session.exists(_.status != Session.Setup)) {

              val numPlayers = {
                if (board.status > 0) players.size
                else board.game.players.max
              }

              board.game.start(numPlayers)

            } else state.session.get.currentState
              .asInstanceOf[board.game.StateT]
          }

          val newSession = Session (
            props.user, props.socket, board, players,
            users, gameState, gameState, rematch, forks
          )

          setState(_.copy(session = Some(newSession), gameState = Some(gameState)))
        }

        case PushActions(actions) => {

          val session = actions.foldLeft(state.session.get) {
            (session, action) =>
              session.takeAction(action.actionId, action.turn)
          }

          setState(_.copy(session = Some(session)))
        }

        case Redirect(board) => window.location.href = board match {
          case Some(board) => gameRoute(board.id)
          case None => "/"
        }
      }
    }
  }
}