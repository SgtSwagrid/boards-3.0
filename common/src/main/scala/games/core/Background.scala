package games.core

import games.core.{Colour, Vec}

trait Background[V <: Vec] {
  def colour(pos: V): Colour
}

object Background {

  case class Checkerboard(c1: Colour, c2: Colour)
      extends Background[Vec2] {
        
    def colour(pos: Vec2) =
      if ((pos.x + pos.y) % 2 == 0) c1 else c2
  }
}