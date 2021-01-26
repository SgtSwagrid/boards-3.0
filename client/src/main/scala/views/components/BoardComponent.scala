package views.components

import org.scalajs.dom._
import org.scalajs.dom.html._
import slinky.core.Component
import slinky.core.annotations.react
import slinky.web.html._
import models.Board
import games.core.Game
import games.core.States._
import games.core.Scene
import org.scalajs.dom.raw.HTMLImageElement

@react class BoardComponent extends Component {

  private val canvasId = "boardCanvas"
  
  case class Props(board: Board, state: AnyState)
  type State = Unit
  val initialState = ()

  def render() = div(className := "canvas-container") (
    canvas(className := "board-canvas", id := canvasId)
  )

  override def componentDidMount() = {
    draw()
    window.addEventListener("resize", {_: Event => draw()}, false)
  }

  private def draw() = {

    val canvas = document.getElementById(canvasId).asInstanceOf[Canvas]
    val context = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]

    val width = canvas.clientWidth
    val height = canvas.clientHeight

    canvas.width = width
    canvas.height = height

    val scene = props.board.scene(props.state)

    val hsf = width / scene.width
    val vsf = height / scene.height
    val sf = hsf min vsf

    val hoffset = (width - scene.width * sf) / 2 - scene.left * sf
    val voffset = (height - scene.height * sf) / 2 - scene.bottom * sf

    scene.tiles map { tile =>

      val x = tile.position.x * sf + hoffset
      val y = height - (tile.position.y * sf + voffset) - tile.size.y * sf

      val size = tile.size * sf
      
      context.fillStyle = tile.colour.hex
      context.fillRect(x, y, size.x, size.y)
    }

    scene.pieces map { piece =>

      val x = piece.position.x * sf + hoffset
      val y = height - (piece.position.y * sf + voffset) - piece.size.y * sf

      val size = piece.size * sf
    
      val image = document.createElement("img").asInstanceOf[HTMLImageElement]
      image.src = "/assets/img/" + piece.texture
      image.onload = (e: Event) => {
        context.drawImage(image, x, y, size.x, size.y)
      }
    }
  }
}