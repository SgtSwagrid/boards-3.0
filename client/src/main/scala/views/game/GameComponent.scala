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

  case class State (
    board: Option[Board],
    players: Seq[Player],
    users: Seq[User],
    currentState: Option[AnyState],
    visibleState: Option[AnyState],
    player: Option[Player]
  )

  def initialState = State(None, Seq(), Seq(), None, None, None)

  def session = BoardSession(props.user, state.player, props.socket)

  def render() = for {
    board <- state.board
    currentState <- state.currentState
    visibleState <- state.visibleState
  } yield {

    div (
      SidebarComponent(board, state.players, state.users,
        currentState.asInstanceOf[board.game.StateT],
        visibleState.asInstanceOf[board.game.StateT],
        session,
        s => setState(_.copy(visibleState = Some(s)))),

      BoardComponent(board,
        visibleState.asInstanceOf[board.game.StateT],
        currentState == visibleState, session)
    )
  }

  override def componentDidMount() =
    props.socket.onmessage = { (e: MessageEvent) =>
      decode[BoardResponse](e.data.toString).toOption.get match {

        case SetBoard(board) => {

          if (!board.isDefined) window.location.href = "/"

          setState(_.copy (
            board = board,
            currentState = board map { board =>
              board.game.start(state.players.size)
            }
          ))
        }

        case SetPlayers(players, users) =>

          setState(_.copy (
            players = players,
            users = users,

            player = (players zip users)
              .find { case (_, user) => user.id == props.user.id }
              .map { case (player, _) => player }
          ))

        case PushActions(actions) => {

          (state.board zip state.currentState) foreach {
            case (board, state) =>

              val oldState = state.asInstanceOf[board.game.StateT]

              val newState = actions.foldLeft(oldState) { 
                (gameState, action) =>

                  val states = board.game.successors(gameState).toSeq

                  if (states.isDefinedAt(action.actionId)
                      && gameState.turn == action.turn)
                    states(action.actionId)
                  else gameState
              }

              setState(_.copy (
                currentState = Some(newState),
                visibleState = Some(newState)
              ))
          }
        }
      }
    }
}