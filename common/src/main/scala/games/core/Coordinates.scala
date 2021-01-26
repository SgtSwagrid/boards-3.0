package games.core

object Coordinates {

  trait Coordinate

  case class Vec2(x: Int, y: Int) extends Coordinate {

    def + (v: Vec2) = Vec2(x + v.x, y + v.y)
    def + (v: (Int, Int)) = Vec2(x + v._1, y + v._2)
    def - (v: Vec2) = Vec2(x - v.x, y - v.y)
    def * (d: Int) = Vec2(x * d, y * d)
    def unary_- = Vec2(-x, -y)
  }

  object Vec2 {

    val zero = Vec2(0, 0)
    val unit = Vec2(1, 1)
    
    val left = Vec2(-1, 0)
    val right = Vec2(1, 0)
    val down = Vec2(0, -1)
    val up = Vec2(0, 1)
  }
}