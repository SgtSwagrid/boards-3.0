package games.core

import scala.math._
import games.core.Coordinates._

object Manifolds {

  trait Manifold[C <: Coordinate] {

    val positions: Seq[C]
    def inBounds(pos: C): Boolean
  }

  case class RectangleManifold(width: Int, height: Int)
      extends Manifold[Vec2] {

    val positions = for {
      x <- 0 until width
      y <- 0 until height
    } yield Vec2(x, y)

    def inBounds(pos: Vec2) =
      pos.x >= 0 && pos.x < width &&
      pos.y >= 0 && pos.y < height

    def taxiDist(p1: Vec2, p2: Vec2) =
      abs(p2.x - p1.x) + abs(p2.y - p1.y)

    def kingsDist(p1: Vec2, p2: Vec2) =
      abs(p2.x - p1.x) max abs(p2.y - p1.y)

    def orthAdjacent(p1: Vec2, p2: Vec2) =
      taxiDist(p1, p2) == 1

    def diagAdjacent(p1: Vec2, p2: Vec2) =
      kingsDist(p1, p2) == 1

    def row(y: Int) =
      (0 until width).map(x => Vec2(x, y))

    def column(x: Int) =
      (0 until height).map(y => Vec2(x, y))
  }

  case class RowManifold(rows: Row*) extends Manifold[Vec2] {

    val positions = for {
      (row, y) <- rows.zipWithIndex
      x <- row.positions
    } yield Vec2(x, y)

    def inBounds(pos: Vec2) =
      pos.y >= 0 && pos.y < rows.size &&
      rows(pos.y).inBounds(pos.x)
    
    def row(y: Int) =
      rows(y).positions.map(x => Vec2(x, y))
    
    def column(x: Int) =
      (0 until rows.size)
        .filter(y => rows(y).inBounds(x))
        .map(y => Vec2(x, y))
  }

  case class Row(sections: Section*) {

    val offsets = (Section(0) +: sections).sliding(2).map {
      case Seq(l, r) => l.size + r.offset
    }.scanLeft(0)(_+_).toSeq.tail

    val positions = for {
      (section, offset) <- (sections zip offsets)
      x <- (0 until section.size).map(_ + offset)
    } yield x

    def inBounds(x: Int) = (sections zip offsets)
      .dropWhile { case (_, o) => o > x }
      .headOption.exists { case (s, o) => x - o < s.size }

    val start = sections.head.offset
    val end = sections.map(s => s.offset + s.size).sum
  }

  case class Section(size: Int = 1, offset: Int = 0)
}