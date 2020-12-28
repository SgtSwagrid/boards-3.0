package games.core

object Coordinates {

  trait Coordinate

  case class Vec2(x: Int, y: Int) extends Coordinate {

    def + (v: Vec2) = Vec2(x + v.x, y + v.y)
    def - (v: Vec2) = Vec2(x - v.x, y - v.y)
    def * (d: Int) = Vec2(x * d, y * d)
    def unary_- = Vec2(-x, -y)
  }
}