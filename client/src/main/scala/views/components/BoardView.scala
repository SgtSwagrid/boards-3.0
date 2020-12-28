package views

import org.scalajs.dom._
import org.scalajs.dom.html
import scala.scalajs.js.annotation.JSExportTopLevel
import slinky.core.{Component, StatelessComponent}
import slinky.core.facade.ReactElement
import slinky.core.annotations.react
import slinky.web.ReactDOM
import slinky.web.html._

import games.core.GameInstance

object BoardView {

  val boardId = document.getElementById("boardId")
    .asInstanceOf[html.Input].value

  @JSExportTopLevel("board")
  def board() = {
    
    ReactDOM.render (
      GameComponent(boardId),
      document.getElementById("root")
    )
  }

  @react class GameComponent extends Component {

    case class Props(boardId: String)
    type State = Option[GameInstance]
    def initialState = None

    def render() = SidebarComponent(props.boardId)
  }

  @react class SidebarComponent extends Component {

    case class Props(boardId: String)
    type State = Option[Int]
    def initialState = None

    def render() = div(className := "sidebar grey darken-2") (
      div(className := "sidebar-header grey darken-3") (
        span(className := "large-text white-text") (boardId)
      )
    )
  }
}