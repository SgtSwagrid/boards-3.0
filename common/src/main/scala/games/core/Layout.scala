package games.core

import games.core.Coordinates.Vec2
import games.core.Layout._

class Layout {

  
}

object Layout {
  
  sealed trait Shape {
    val pos: Vec2
    val size: Vec2
  }

  case class Rectangle (
    pos: Vec2,
    size: Vec2 = Vec2(1, 1)
  ) extends Shape

  case class Triangle (
    pos: Vec2,
    size: Vec2 = Vec2(2, 2),
    orientation: Boolean = false
  ) extends Shape

  case class Hexagon(
    pos: Vec2,
    size: Vec2 = Vec2(2, 2),
    orientation: Boolean = false
  ) extends Shape
}