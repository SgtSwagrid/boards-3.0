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

  case class Props (
    boardId: String,
    user: User,
    socket: WebSocket
  )

  case class State(session: Option[Session.AnySession] = None)

  def initialState = State()

  def render() = state.session.map { session => div (

    SidebarComponent(session,
      s => setState(_.copy(session = Some(s)))),
    BoardComponent(session)
  )}


  override def componentDidMount() =
    props.socket.onmessage = { (e: MessageEvent) =>
      decode[BoardResponse](e.data.toString).toOption.get match {

        case SetBoard(board) => {

          board match {

            case Some(board) => {
          
              val session = state.session.map { session =>
                session.setBoard(board)
              }.getOrElse(Session(props.user, props.socket, board))

              setState(_.copy(session = Some(session)))
            }

            case None => window.location.href = "/"
          }
        }

        case SetPlayers(players, users) => {

          val session = state.session.get.setPlayers(players, users)
          setState(_.copy(session = Some(session)))
        }

        case PushActions(actions) => {

          val session = actions.foldLeft(state.session.get) {
            (session, action) =>
              session.takeAction(action.actionId, action.turn)
          }

          setState(_.copy(session = Some(session)))
        }
      }
    }
}