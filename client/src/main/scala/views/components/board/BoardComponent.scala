package views.components.board

import org.scalajs.dom._
import org.scalajs.dom.html.Canvas
import org.scalajs.dom.raw.HTMLImageElement

import slinky.core.Component
import slinky.core.annotations.react
import slinky.web.html
import slinky.web.html._

import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

import models.Board
import models.protocols.BoardProtocol._
import games.core.{Colour, Game, InputAction, State, Vec, Vec2}
import games.core.State.AnyState
import views.components.BoardView.BoardSession
import views.components.board.ImageCache

@react class BoardComponent extends Component {
  
  private val canvasId = "boardCanvas"

  case class Props (
    board: Board,
    gameState: AnyState,
    session: BoardSession
  )

  case class State (
    canvasSize: Vec2 = Vec2.zero,
    cursor: Vec2 = Vec2.zero,
    selected: Option[Vec] = None,
    drag: Boolean = false
  )

  def initialState = State()

  def render() = div(className := "canvas-container") (
    html.canvas(className := "board-canvas", id := canvasId)
  )

  private lazy val canvas = document.getElementById(canvasId)
    .asInstanceOf[Canvas]
  
  private lazy val context = canvas.getContext("2d")
    .asInstanceOf[CanvasRenderingContext2D]

  private val images = new ImageCache()

  private lazy val game = props.board.game
  private lazy val layout = game.layout
  private lazy val background = game.background
  
  private def gameState = props.gameState.asInstanceOf[StateT]

  private type VecT = game.VecT
  private type StateT = game.StateT
  private type SceneT = Scene[VecT]
  
  private def selected = state.selected.asInstanceOf[Option[VecT]]

  override def componentDidMount() = {

    setState(_.copy(canvasSize =
      Vec2(canvas.clientWidth, canvas.clientHeight)))

    window.addEventListener("resize", { _: Event =>
      setState(_.copy(canvasSize =
        Vec2(canvas.clientWidth, canvas.clientHeight)))
    })

    canvas.onmousemove = e => setState(_.copy(cursor = cursorPos(e)))
    canvas.onmousedown = e => mouseDown(cursorPos(e))
    canvas.onmouseup = e => mouseUp(cursorPos(e))
  }

  private def mouseDown(pos: Vec2) = {

    val scene = new SceneT(game, gameState, state.canvasSize)

    if (props.session.player.exists(_.turnOrder == gameState.turn)
        && props.board.ongoing) {

      val moved = (selected zip scene.location(pos)) exists {
        case (from, to) => tryMove(from, to)
      }

      if (!moved) {

        val loc = scene.location(pos).filter { loc =>
          game.moves(gameState, loc).nonEmpty
        }

        setState(_.copy(selected = loc, drag = true))
      }
    }
  }

  private def mouseUp(pos: Vec2) = {

    setState(_.copy(drag = false))

    val scene = new SceneT(game, gameState, state.canvasSize)

    (selected zip scene.location(pos)) foreach {
      case (from, to) =>  tryMove(from, to)
    }
  }

  private def tryMove(from: VecT, to: VecT) = {

    val move = InputAction.Move(from, to)
    val valid = game.validateAction(gameState, move)
      
    if (valid) {
      takeAction(move)
      setState(_.copy(selected = None))
    }

    valid
  }

  private def takeAction(action: InputAction) {

    val actionId = game.actions(gameState).indexOf(action)
    val request: BoardRequest = TakeAction(props.board.id, actionId)
    if (actionId != -1) props.session.socket.send(request.asJson.toString)
  }

  private def cursorPos(event: MouseEvent) = {

    val rect = canvas.getBoundingClientRect()
    val x = (event.clientX - rect.left).toInt
    val y = (event.clientY - rect.top).toInt
    Vec2(x, y)
  }

  override def componentDidUpdate(prevProps: Props, prevState: State) = {

    val scene = new SceneT(game, gameState, state.canvasSize)

    canvas.width = canvas.clientWidth
    canvas.height = canvas.clientHeight

    draw(scene)
  }

  private def draw(scene: SceneT) = {

    scene.locations.foreach(drawTile(scene, _))

    selected foreach { loc =>
      drawHints(scene, loc)
      if (state.drag) drawDrag(scene, loc)
    }
  }

  private def drawTile(scene: SceneT, loc: VecT) = {

    val Vec2(x, y) = scene.position(loc)
    val Vec2(width, height) = scene.size(loc)
    val colour = background.colour(loc)

    context.fillStyle =
      if (selected == Some(loc)) colour.darken(50).hex
      else if (scene.location(state.cursor) == Some(loc)) colour.darken(25).hex
      else colour.hex

    context.fillRect(x, y, width, height)
    
    gameState.pieces.get(loc) foreach { piece =>
      if (!(selected == Some(loc) && state.drag)) {

        val image = images.image(piece.texture)
        context.drawImage(image, x, y, width, height)
      }
    }
  }

  private def drawHints(scene: SceneT, loc: VecT) = {

    game.moves(gameState, loc) foreach { move =>
      
      val Vec2(x, y) = scene.position(move.to) + scene.size(move.to) / 2
      
      val tileSize = scene.size(move.to)
      val size = (tileSize.x min tileSize.y) / 2

      if (gameState.pieces.isDefinedAt(move.to)) {

        context.strokeStyle = Colour.fusionRed.hex
        context.globalAlpha = 0.8
        context.lineWidth = size * 0.25
        context.beginPath()
        context.arc(x, y, size * 0.8, 0, 2 * Math.PI)
        context.stroke()

      } else {

        context.fillStyle = Colour.fusionRed.hex
        context.globalAlpha = 0.8
        context.beginPath()
        context.arc(x, y, size * 0.4, 0, 2 * Math.PI)
        context.fill()
      }
    }
  }

  private def drawDrag(scene: SceneT, loc: VecT) = {

    val Vec2(width, height) = scene.size(loc)
    val piece = gameState.pieces(loc)
    val image = images.image(piece.texture)

    val Vec2(mx, my) = state.cursor
    context.globalAlpha = 0.8
    context.drawImage(image, mx - width/2, my - height/2, width, height)
  }
}