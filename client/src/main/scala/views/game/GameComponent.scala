package views.game

import org.scalajs.dom._

import slinky.core.{Component, StatelessComponent}
import slinky.core.facade.ReactElement
import slinky.core.annotations.react
import slinky.web.ReactDOM
import slinky.web.html._

import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

import models.{Board, Participant, Player, User}
import models.protocols.BoardProtocol._
import games.core.History

@react class GameComponent extends Component {

    case class Props (
      boardId: String,
      user: User,
      socket: WebSocket
    )

    case class State (
      board: Option[Board],
      players: Seq[Participant],
      player: Option[Player],
      gameState: Option[Any]
    )

    def initialState = State(None, Seq(), None, None)

    def session = BoardSession(props.user, state.player, props.socket)

    def render() = (state.board zip state.gameState) map {
      case (board, gs) =>
        val gameState = gs.asInstanceOf[board.game.HistoryT]
        div (
          SidebarComponent(board, state.players, session),
          BoardComponent(board, gameState, session)
        )
    }

    override def componentDidMount() =
      props.socket.onmessage = { (e: MessageEvent) =>
        decode[BoardResponse](e.data.toString).toOption.get match {

          case SetBoard(board) =>
            setState(_.copy (
              board = board,
              gameState = board map { board =>
                History(board.game.start(state.players.size))
              }
            ))

          case SetPlayers(players) =>
            setState(_.copy (
              players = players,
              player = players.find(_.user.id == props.user.id).map(_.player)
            ))

          case PushActions(actions) => {

            (state.board zip state.gameState) foreach {
              case (board, gameState) =>

                val oldState = gameState.asInstanceOf[board.game.HistoryT]

                val newState = actions.foldLeft(oldState) { 
                  (gameState, action) =>

                    val states = board.game.next(gameState).toSeq

                    if (states.isDefinedAt(action.actionId)
                        && gameState.state.turn == action.turn)
                      states(action.actionId)
                    else gameState
                }

                setState(_.copy(gameState = Some(newState)))
            }
          }
        }
      }
  }

  @react class NoGameComponent extends StatelessComponent {

    type Props = Unit

    def render() = div("No board.")
  }