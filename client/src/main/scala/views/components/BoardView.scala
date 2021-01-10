package views

import org.scalajs.dom._, org.scalajs.dom.html
import scala.scalajs.js.annotation.JSExportTopLevel
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import slinky.core.{Component, StatelessComponent}
import slinky.core.facade.ReactElement
import slinky.core.annotations.react
import slinky.web.ReactDOM, slinky.web.html._
import models.{Board, Player, User, Participant}, requests.BoardRequests._

object BoardView {

  case class BoardSession(user: User, player: Option[Player], socket: WebSocket)

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

    val board = FetchJson.getJson(detailsRoute + boardId) { board: Board =>
      ReactDOM.render (
        StateComponent(board, user, socket),
        document.getElementById("root")
      )
    }
  }

  @react class StateComponent extends Component {

    case class Props (
      board: Board,
      user: User,
      socket: WebSocket
    )

    case class State (
      players: Seq[Participant],
      player: Option[Player]
    )

    def initialState = State(Seq(), None)

    def render() = GameComponent(props.board, state.players,
      BoardSession(props.user, state.player, props.socket)
    )

    override def componentDidMount() =
      props.socket.onmessage = { (e: MessageEvent) =>
        decode[BoardResponse](e.data.toString).toOption.get match {

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
        props.players.map(player => PlayerComponent(player, props.session)),
        br(),
        props.session.player map { player =>
          a(className := buttonClass, onClick := (_ => leave())) (
            img(className := "icon", src := "/assets/img/remove.svg"),
            "Leave Game"
          )
        } getOrElse {
          a(className := buttonClass, onClick := (_ => join())) (
            img(className := "icon", src := "/assets/img/enter.svg"),
            "Join Game"
          )
        }
      )
    )

    private def join() = {
      send(JoinGame(props.board.id, props.session.user.id), props.session)
    }

    private def leave() = {
      send(LeaveGame(props.board.id, props.session.user.id), props.session)
    }
  }

  @react class PlayerComponent extends StatelessComponent {

    case class Props(player: Participant, session: BoardSession)

    def render() = div(className := "sidebar-player") (
      span(className := "medium-text white-text") (
        img(className := "icon", src := "/assets/img/user.svg"),
        props.player.user.username
      )
    )
  }

  private def send(req: BoardRequest, session: BoardSession) {
    session.socket.send(req.asJson.toString)
  }
}