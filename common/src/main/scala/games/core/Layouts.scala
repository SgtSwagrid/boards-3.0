package games.core

import games.core.Coordinates._

object Layouts {

  trait Layout[C <: Coordinate] {
    
    def position(pos: C): Vec2
    def size(pos: C): Vec2
    def shape(pos: C): Shape
  }

  case object GridLayout extends Layout[Vec2] {

    def position(pos: Vec2) = pos
    def size(pos: Vec2) = Vec2.unit
    def shape(pos: Vec2) = Rectangle
  }

  sealed trait Shape
  case object Rectangle extends Shape
  case object Triangle extends Shape
  case object Hexagon extends Shape
}