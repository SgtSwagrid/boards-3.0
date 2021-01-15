package views.components

import org.scalajs.dom._, org.scalajs.dom.html
import scala.scalajs.js.annotation.JSExportTopLevel
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import slinky.core.{Component, StatelessComponent}
import slinky.core.facade.ReactElement
import slinky.core.annotations.react
import slinky.web.ReactDOM, slinky.web.html._
import models.{Board, Player, User, Participant}, requests.BoardRequests._
import views.components.ButtonComponent

object BoardView {

  case class BoardSession(user: User, player: Option[Player], socket: WebSocket) {
    def owner = player.map(_.isOwner).getOrElse(false)
  }

  val createRoute  = "/games/create/"
  val gameRoute    = "/games/board/"
  val detailsRoute = "/games/details/"
  val joinRoute    = "/games/join/"
  val leaveRoute   = "/games/leave/"

  lazy val socketRoute = document.getElementById("socketRoute")
    .asInstanceOf[html.Input].value.replace("http", "ws")

  val buttonClass = "btn yellow darken-2 black-text waves-effect block"

  @JSExportTopLevel("board")
  def board() = {

    val boardId = document.getElementById("boardId")
      .asInstanceOf[html.Input].value

    val user = decode[User](document.getElementById("user")
      .asInstanceOf[html.Input].value).toOption.get

    val socket = new WebSocket(socketRoute)

    ReactDOM.render (
      StateComponent(boardId, user, socket),
      document.getElementById("root")
    )
  }

  @react class StateComponent extends Component {

    case class Props (
      boardId: String,
      user: User,
      socket: WebSocket
    )

    case class State (
      board: Option[Board],
      players: Seq[Participant],
      player: Option[Player]
    )

    def initialState = State(None, Seq(), None)

    def render() = state.board.map { board =>
      GameComponent(board, state.players,
        BoardSession(props.user, state.player, props.socket)
      )
    }

    override def componentDidMount() =
      props.socket.onmessage = { (e: MessageEvent) =>
        println(e.data.toString)
        decode[BoardResponse](e.data.toString).toOption.get match {

          case SetBoard(board, _) =>
            setState(s => s.copy(board = board))

          case SetPlayers(players, _) =>
            setState(s => s.copy (
              players = players,
              player = players.find(_.user.id == props.user.id).map(_.player)
            ))
        }
      }
  }

  @react class GameComponent extends Component {

    case class Props(board: Board, players: Seq[Participant], session: BoardSession)
    type State = Unit
    def initialState = ()

    def render() = SidebarComponent(props.board, props.players, props.session)
  }

  @react class SidebarComponent extends Component {

    case class Props(board: Board, players: Seq[Participant], session: BoardSession)
    type State = Unit
    def initialState = ()

    def render() = div(className := "sidebar grey darken-2") (
      div(className := "sidebar-header grey darken-3") (
        div(className := "medium-text white-text") (props.board.game.name),
        div(className := "small-text grey-text") ("#" + props.board.id)
      ),
      div(className := "sidebar-body") (
        props.players.map(player => PlayerComponent(props.board, player, props.session)),
        br(),

        if (props.board.setup) div (

          Option.when(props.session.owner) { div (
            Option.when(props.board.game.players.contains(props.players.size)) {
              ButtonComponent("Start Game", "/assets/img/play.svg", start)
            },
            ButtonComponent("Cancel Game", "/assets/img/trash.svg", delete)
          )},

          props.session.player match {
            case Some(player) =>
              ButtonComponent("Leave Game", "/assets/img/cancel.svg", leave)
            case None =>
              ButtonComponent("Join Game", "/assets/img/join.svg", join)
          }

        ) else if (props.board.ongoing) div (

          

        ) else if (props.board.ended) div (

          Option.when(props.session.owner) {
            ButtonComponent("Delete Game", "/assets/img/remove.svg", delete)
          }

        ) else div()
      )
    )

    private def join() =
      send(JoinGame(props.board.id, props.session.user.id), props.session)

    private def leave() =
      send(RemovePlayer(props.board.id, props.session.player.get.id), props.session)

    private def delete() =
      send(DeleteGame(props.board.id), props.session)

    private def start() =
      send(StartGame(props.board.id), props.session)
  }

  @react class PlayerComponent extends StatelessComponent {

    case class Props(board: Board, player: Participant, session: BoardSession)

    def render() = div(className := "sidebar-player") (
      span(className := "medium-text white-text") (
        if (props.player.player.isOwner) {
          img(className := "medium-icon", src := "/assets/img/owner.svg")
        } else {
          img(className := "medium-icon", src := "/assets/img/user.svg")
        },
        props.player.user.username
      ),
      Option.when(props.session.owner && props.board.setup) {
        span(className := "player-options") (
          a(className := "yellow darken-2 waves-effect player-promote",
              onClick := (_ => promote())) (
            img(className := "order-icon", src := "/assets/img/up-arrow.svg")
          ),
          a(className := "yellow darken-2  waves-effect player-demote",
              onClick := (_ => demote())) (
            img(className := "order-icon", src := "/assets/img/down-arrow.svg")
          ),
          a(className := "red darken-4 waves-effect player-remove",
              onClick := (_ => remove())) (
            img(className := "remove-icon", src := "/assets/img/remove.svg")
          )
        )
      }
    )

    private def remove() =
      send(RemovePlayer(props.board.id, props.player.player.id), props.session)

    private def promote() =
      send(PromotePlayer(props.board.id, props.player.player.id), props.session)

    private def demote() =
      send(DemotePlayer(props.board.id, props.player.player.id), props.session)
  }

  @react class NoGameComponent extends StatelessComponent {

    type Props = Unit

    def render() = div("No board.")
  }

  private def send(req: BoardRequest, session: BoardSession) {
    session.socket.send(req.asJson.toString)
  }
}