package games.core

import games.core.Coordinates._
import games.core.Colour

object Backgrounds {
  
  trait Background[C <: Coordinate] {
    def colour(pos: C): Colour
  }

  case class Checkerboard(c1: Colour, c2: Colour)
      extends Background[Vec2] {
        
    def colour(pos: Vec2) =
      if ((pos.x + pos.y) % 2 == 0) c1 else c2
  }
}
