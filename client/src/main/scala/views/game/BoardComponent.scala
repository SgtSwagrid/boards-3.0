package views.game

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
import games.core.{Action, ActionSet, Colour, Game, Piece, State, Vec, Vec2}
import games.core.State.{AnyState, Ongoing}

@react class BoardComponent extends Component {
  
  private val canvasId = "boardCanvas"

  case class Props(session: Session[_ <: Vec, _ <: Piece])
  private lazy val game = props.session.game
  private def session = props.session
    .asInstanceOf[Session[game.VecT, game.PieceT]]
  
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

  private def layout = game.layout(props.session.player.map(_.turnOrder))
  private lazy val background = game.background
  
  private def selected = state.selected
    .asInstanceOf[Option[game.VecT]]

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

    if (session.canPlay) {

      val scene = new Scene(game, session.visibleState, layout, state.canvasSize)

      val placed = scene.location(pos).exists(tryPlace)

      val moved = (selected zip scene.location(pos)) exists {
        case (from, to) => tryMove(from, to)
      }

      if (!placed && !moved) {

        val loc = scene.location(pos).filter { loc =>
          session.movesFrom.get(loc).nonEmpty
        }

        setState(_.copy(selected = loc, drag = true))
      }
      autoSelect(session.sortedActions)
        .foreach(s => setState(_.copy(selected = Some(s))))
    }
  }

  private def mouseUp(pos: Vec2) = {

    setState(_.copy(drag = false))

    val scene = new Scene(game, session.visibleState, layout, state.canvasSize)

    (selected zip scene.location(pos)) foreach {
      case (from, to) => tryMove(from, to)
    }
  }

  private def tryPlace(pos: game.VecT) = {

    val place = session.placesAt.get(pos).headOption
    place.foreach(takeAction)
    place.isDefined
  }

  private def tryMove(from: game.VecT, to: game.VecT) = {

    val move = Action.Move(from, to)
    val valid = session.actions.contains(move)
      
    if (valid) {
      takeAction(move)
      setState(_.copy(selected = None))
    }

    valid
  }

  private def takeAction(action: Action[game.VecT]) {

    val actionId = session.sortedActions.indexOf(action)
    
    if (actionId != -1) {
      val request: BoardRequest = TakeAction(session.board.id, actionId)
      props.session.socket.send(request.asJson.toString)
    }
  }

  private def cursorPos(event: MouseEvent) = {

    val rect = canvas.getBoundingClientRect()
    val x = (event.clientX - rect.left).toInt
    val y = (event.clientY - rect.top).toInt
    Vec2(x, y)
  }

  private def autoSelect(actions: Seq[Action[game.VecT]]) = {

    if (session.canPlay)
      actions match {
        case Action.Move(from, _) :: actions
          if actions.forall {
            case Action.Move(`from`, _) => true
            case _ => false
          } => Some(from)
        case _ => None
      }
    else None
  }

  override def componentDidUpdate(prevProps: Props, prevState: State) = {
    
    if (props != prevProps) {

      if (session.canPlay) {

        setState(_.copy (
          selected = autoSelect(session.sortedActions),
          drag = false
        ))

      } else {
        setState(_.copy (selected = None, drag = false))
      }
    }

    val scene = new Scene(game, session.visibleState, layout, state.canvasSize)

    canvas.width = canvas.clientWidth
    canvas.height = canvas.clientHeight

    draw(scene)
  }

  private def draw(scene: Scene[game.VecT]): Unit = {

    scene.locations.foreach(drawTile(scene, _))
    
    if (session.canPlay) drawHints(scene)
    
    selected foreach { loc =>   
      if (state.drag) drawDrag(scene, loc)
    }
  }

  private def drawTile(scene: Scene[game.VecT], loc: game.VecT) = {

    val Vec2(x, y) = scene.position(loc)
    val Vec2(width, height) = scene.size(loc)
    val colour = background.colour(loc)

    context.fillStyle = tileColour(scene, loc).hex

    context.fillRect(x, y, width, height)
    
    session.visibleState.pieces.get(loc) foreach { piece =>
      if (!(selected == Some(loc) && state.drag)) {

        val image = images.image(piece.texture)
        image.onload = e => draw(scene)
        context.drawImage(image, x, y, width, height)
      }
    }
  }

  private def tileColour(scene: Scene[game.VecT], loc: game.VecT) = {

    val base = background.colour(loc)

    val changes = session.visibleState.actionsThisTurn flatMap {
      case Action.Place(pos, _) => Seq(pos)
      case Action.Move(from, to) => Seq(from, to)
      case Action.Destroy(pos) => Seq(pos)
      case _ => None
    }

    val highlighted =
      if (state.selected.contains(loc))
        Colour.mix(base, Colour.naval)(1, 1)
      else if (changes.contains(loc))
        Colour.mix(base, Colour.downloadProgess)(1, 1)
      else base

    if (scene.location(state.cursor) == Some(loc))
      highlighted.darken(25) else highlighted
  }

  private def drawHints(scene: Scene[game.VecT]) = {

    val locations = selected match {
      case Some(selected) => session.movesFrom.get(selected).map(_.to)
      case None => session.places.map(_.pos)
    }

    locations foreach { loc =>
      
      val Vec2(x, y) = scene.position(loc) + scene.size(loc) / 2
      
      val tileSize = scene.size(loc)
      val size = (tileSize.x min tileSize.y) / 2

      if (session.visibleState.pieces.isDefinedAt(loc)) {

        context.strokeStyle = Colour.naval.hex
        context.globalAlpha = 0.8
        context.lineWidth = size * 0.2
        context.beginPath()
        context.arc(x, y, size * 0.8, 0, 2 * Math.PI)
        context.stroke()

      } else {

        context.fillStyle = Colour.naval.hex
        context.globalAlpha = 0.8
        context.beginPath()
        context.arc(x, y, size * 0.35, 0, 2 * Math.PI)
        context.fill()
      }
    }
  }

  private def drawDrag(scene: Scene[game.VecT], loc: game.VecT) = {

    val Vec2(width, height) = scene.size(loc)
    val piece = session.visibleState.pieces(loc)
    val image = images.image(piece.texture)

    val Vec2(mx, my) = state.cursor
    context.globalAlpha = 0.8
    context.drawImage(image, mx - width/2, my - height/2, width, height)
  }
}