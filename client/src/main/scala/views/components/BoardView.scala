package views

import org.scalajs.dom._
import org.scalajs.dom.html
import scala.scalajs.js.annotation.JSExportTopLevel
import slinky.core.{Component, StatelessComponent}
import slinky.core.facade.ReactElement
import slinky.core.annotations.react
import slinky.web.ReactDOM
import slinky.web.html._

import games.core.GameInstance._

object BoardView {

  val createRoute  = "/games/create/"
  val gameRoute    = "/games/board/"
  val detailsRoute = "/games/details/"
  val joinRoute    = "/games/join/"
  val leaveRoute   = "/games/leave/"

  val buttonClass = "btn yellow darken-2 black-text waves-effect block"

  @JSExportTopLevel("board")
  def board() = {

    val boardId = document.getElementById("boardId")
      .asInstanceOf[html.Input].value

    val userId = document.getElementById("userId")
      .asInstanceOf[html.Input].value.toInt

    val board = FetchJson.getJson(detailsRoute + boardId) { board: Board =>
      ReactDOM.render (
        GameComponent(board, userId),
        document.getElementById("root")
      )
    }
  }

  @react class GameComponent extends Component {

    case class Props(board: Board, userId: Int)
    type State = Unit
    def initialState = ()

    def render() = SidebarComponent(props.board, props.userId)
  }

  @react class SidebarComponent extends Component {

    case class Props(board: Board, userId: Int)
    type State = Unit
    def initialState = ()

    def render() = div(className := "sidebar grey darken-2") (
      div(className := "sidebar-header grey darken-3") (
        div(className := "medium-text white-text") (props.board.game.name),
        div(className := "small-text grey-text") ("#" + props.board.id)
      ),
      div(className := "sidebar-body") (
        props.board.players map { player =>
          PlayerComponent(player)
        },
        br(),
        if (!props.board.players.exists(_.userId == Some(props.userId))) {
          a(className := buttonClass, onClick := (_ => join())) (
            img(className := "icon", src := "/assets/img/enter.svg"),
            "Join Game"
          )
        } else {
          a(className := buttonClass, onClick := (_ => leave())) (
            img(className := "icon", src := "/assets/img/remove.svg"),
            "Leave Game"
          )
        }
      )
    )

    private def join() = {
      FetchJson.post[Boolean](joinRoute + props.board.id)(_ => {})
    }

    private def leave() = {
      FetchJson.post[Boolean](leaveRoute + props.board.id)(_ => {})
    }
  }

  @react class PlayerComponent extends StatelessComponent {

    case class Props(player: Player)

    def render() = div(className := "sidebar-player") (
      span(className := "medium-text white-text") (
        img(className := "icon", src := "/assets/img/user.svg"),
        props.player.username
      )
    )
  }
}