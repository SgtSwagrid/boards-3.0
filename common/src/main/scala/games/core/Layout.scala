package games.core

import games.core.{Vec, Vec2}

trait Layout[V <: Vec] {
  
  def position(pos: V): Vec2
  def size(pos: V): Vec2
  def shape(pos: V): Layout.Shape
}

object Layout {

  case object Grid extends Layout[Vec2] {

    def position(pos: Vec2) = pos
    def size(pos: Vec2) = Vec2(1, 1)
    def shape(pos: Vec2) = Rectangle
  }

  case object RotatedGrid extends Layout[Vec2] {

    def position(pos: Vec2) = -pos
    def size(pos: Vec2) = Vec2(1, 1)
    def shape(pos: Vec2) = Rectangle
  }

  sealed trait Shape
  case object Rectangle extends Shape
  case object Triangle extends Shape
  case object Hexagon extends Shape
}