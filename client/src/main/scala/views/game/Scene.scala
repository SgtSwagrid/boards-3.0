package views.game

import games.core.{Background, Colour, Game, Layout, Manifold, State, Vec, Vec2}
import slinky.web.html.height

class Scene[V <: Vec] (
  val game: Game,
  val state: State.AnyState,
  val canvasSize: Vec2
) {

  val manifold = game.manifold.asInstanceOf[Manifold[V]]
  val layout = game.layout.asInstanceOf[Layout[V]]
  val background = game.background.asInstanceOf[Background[V]]

  val locations = manifold.positions

  val left = locations.map(layout.position(_).x).min
  val right = locations.map(p => layout.position(p).x + layout.size(p).x).max
  val bottom = locations.map(layout.position(_).y).min
  val top = locations.map(p => layout.position(p).y + layout.size(p).y).max

  val width = right - left
  val height = top - bottom

  val sf = (canvasSize.x / width) min (canvasSize.y / height)

  val xstart = (canvasSize.x - width * sf) / 2 - left * sf
  val ystart = (canvasSize.y - height * sf) / 2 - bottom * sf

  def position(loc: V) = {

    val position = layout.position(loc)
    val size = layout.size(loc)

    val x = position.x * sf + xstart
    val y = canvasSize.y - (position.y * sf + ystart) - size.y * sf
    Vec2(x, y)
  }

  def size(pos: V) = {
    layout.size(pos) * sf
  }

  def location(pos: Vec2) = {
    locations.find(inBounds(_, pos))
  }

  def inBounds(loc: V, pos: Vec2) = {
    val start = position(loc)
    val end = size(loc) + start
    pos.x > start.x && pos.x < end.x && pos.y > start.y && pos.y < end.y
  }
}