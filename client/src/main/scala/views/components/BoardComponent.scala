package views.components

import org.scalajs.dom._
import slinky.core.Component
import slinky.core.annotations.react
import slinky.web.html._
import models.Board
import games.core.States.AnyState

@react class BoardComponent extends Component {

  private val canvasId = "boardCanvas"
  
  case class Props(board: Board, state: AnyState)
  type State = Unit
  val initialState = ()

  def render() = div(className := "canvas-container") (
    canvas(className := "board-canvas", id := canvasId)
  )

  def componentDidMound() = {

    val canvas = document.getElementById(canvasId)
  }
}