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
import games.core.{Action, Colour, Game, State, Vec, Vec2}
import games.core.State.{AnyState, Ongoing}
import games.core.Piece

@react class BoardComponent extends Component {
  
  private val canvasId = "boardCanvas"

  case class Props (
    board: Board,
    gameState: AnyState,
    current: Boolean,
    session: BoardSession
  )

  case class State (
    canvasSize: Vec2 = Vec2.Zero,
    cursor: Vec2 = Vec2.Zero,
    selected: Option[Vec] = None,
    drag: Boolean = false,
    actions: ActionCache[_ <: Vec, _ <: Piece] = null
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
  private def layout = game.layout(props.session.player.map(_.turnOrder))
  private lazy val background = game.background
  
  private def gameState = props.gameState.asInstanceOf[StateT]

  private def actions = state.actions
    .asInstanceOf[ActionCache[VecT, game.PieceT]]

  private type VecT = game.VecT
  private type StateT = game.StateT
  private type SceneT = Scene[VecT]
  
  private def selected = state.selected.asInstanceOf[Option[VecT]]

  private def ongoing = props.board.ongoing &&
    gameState.outcome == Ongoing

  private def myTurn = props.session.player
    .exists(_.turnOrder == props.gameState.turn)

  private def canPlay = ongoing && myTurn && props.current

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

    setState(_.copy(actions = new ActionCache(game.actions(gameState))))
  }

  private def mouseDown(pos: Vec2) = {

    if (canPlay) {

      val scene = new SceneT(game, gameState, layout, state.canvasSize)

      val placed = scene.location(pos).exists(tryPlace)

      val moved = (selected zip scene.location(pos)) exists {
        case (from, to) => tryMove(from, to)
      }

      if (!placed && !moved) {

        val loc = scene.location(pos).filter { loc =>
          actions.movesFrom.get(loc).nonEmpty
        }

        setState(_.copy(selected = loc, drag = true))
      }
      autoSelect(actions.actionSeq)
        .foreach(s => setState(_.copy(selected = Some(s))))
    }
  }

  private def mouseUp(pos: Vec2) = {

    setState(_.copy(drag = false))

    val scene = new SceneT(game, gameState, layout, state.canvasSize)

    (selected zip scene.location(pos)) foreach {
      case (from, to) => tryMove(from, to)
    }
  }

  private def tryPlace(pos: VecT) = {

    val place = actions.placesAt.get(pos).headOption
    place.foreach(takeAction)
    place.isDefined
  }

  private def tryMove(from: VecT, to: VecT) = {

    val move = Action.Move(from, to)
    val valid = actions.actions.contains(move)
      
    if (valid) {
      takeAction(move)
      setState(_.copy(selected = None))
    }

    valid
  }

  private def takeAction(action: Action[VecT]) {

    val actionId = actions.indexOf(action)
    
    if (actionId != -1) {
      val request: BoardRequest = TakeAction(props.board.id, actionId)
      props.session.socket.send(request.asJson.toString)
    }
  }

  private def cursorPos(event: MouseEvent) = {

    val rect = canvas.getBoundingClientRect()
    val x = (event.clientX - rect.left).toInt
    val y = (event.clientY - rect.top).toInt
    Vec2(x, y)
  }

  private def autoSelect(actions: Seq[Action[VecT]]) = {

    if (canPlay)
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

      val actions = new ActionCache(game.actions(gameState))

      setState(_.copy (
        actions = actions,
        selected = autoSelect(actions.actionSeq),
        drag = false
      ))
    }

    val scene = new SceneT(game, gameState, layout, state.canvasSize)

    canvas.width = canvas.clientWidth
    canvas.height = canvas.clientHeight

    draw(scene)
  }

  private def draw(scene: SceneT): Unit = {

    scene.locations.foreach(drawTile(scene, _))
    
    if (canPlay) drawHints(scene)
    
    selected foreach { loc =>   
      if (state.drag) drawDrag(scene, loc)
    }
  }

  private def drawTile(scene: SceneT, loc: VecT) = {

    val Vec2(x, y) = scene.position(loc)
    val Vec2(width, height) = scene.size(loc)
    val colour = background.colour(loc)

    context.fillStyle = tileColour(scene, loc).hex

    context.fillRect(x, y, width, height)
    
    gameState.pieces.get(loc) foreach { piece =>
      if (!(selected == Some(loc) && state.drag)) {

        val image = images.image(piece.texture)
        image.onload = e => draw(scene)
        context.drawImage(image, x, y, width, height)
      }
    }
  }

  private def tileColour(scene: SceneT, loc: VecT) = {

    val base = background.colour(loc)

    val changes = gameState.actionsThisTurn flatMap {
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

  private def drawHints(scene: SceneT) = {

    val locations = selected match {
      case Some(selected) => actions.movesFrom.get(selected).map(_.to)
      case None => actions.places.map(_.pos)
    }

    println(selected, locations)

    locations foreach { loc =>
      
      val Vec2(x, y) = scene.position(loc) + scene.size(loc) / 2
      
      val tileSize = scene.size(loc)
      val size = (tileSize.x min tileSize.y) / 2

      if (gameState.pieces.isDefinedAt(loc)) {

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

  private def drawDrag(scene: SceneT, loc: VecT) = {

    val Vec2(width, height) = scene.size(loc)
    val piece = gameState.pieces(loc)
    val image = images.image(piece.texture)

    val Vec2(mx, my) = state.cursor
    context.globalAlpha = 0.8
    context.drawImage(image, mx - width/2, my - height/2, width, height)
  }
}