package games.core

import games.core.States._
import games.core.Coordinates._
import games.core.Layouts._
import games.core.Scene._

class Scene[C <: Coordinate](game: Game[C], state: CState[C]) {
  
  val tiles = game.manifold.positions map {
    pos => Tile (
      game.layout.position(pos),
      game.layout.size(pos),
      game.layout.shape(pos),
      game.background.colour(pos)
    )
  }

  val pieces = state.pieces map {
    case (pos, piece) => PieceImage (
      game.layout.position(pos),
      game.layout.size(pos),
      piece.texture
    )
  }

  val left = tiles.map(_.position.x).min
  val right = tiles.map(t => t.position.x + t.size.x).max
  val bottom = tiles.map(_.position.y).min
  val top = tiles.map(t => t.position.y + t.size.y).max

  val width = right - left
  val height = top - bottom
}

object Scene {

  sealed trait Renderable

  case class Tile (
    position: Vec2,
    size: Vec2,
    shape: Shape,
    colour: Colour
  ) extends Renderable

  case class PieceImage (
    position: Vec2,
    size: Vec2,
    texture: String
  ) extends Renderable
}