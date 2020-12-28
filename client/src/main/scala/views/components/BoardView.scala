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

  @JSExportTopLevel("board")
  def board() = {

    val boardId = document.getElementById("boardId")
      .asInstanceOf[html.Input].value
    
    ReactDOM.render (
      GameComponent(boardId),
      document.getElementById("root")
    )
  }

  @react class GameComponent extends Component {

    case class Props(boardId: String)
    type State = Option[GameInstance]
    def initialState = None

    def render() = div()
  }
}