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
  }

  case class RowManifold(rows: Row*) extends Manifold[Vec2] {

    val positions = for {
      (row, y) <- rows.zipWithIndex
      (section, offset) <- row.sections zip row.offsets
      x <- (0 until section.size).map(_ + offset)
    } yield Vec2(x, y)

    def inBounds(pos: Vec2) =
      pos.y >= 0 && pos.y < rows.size &&
      rows(pos.y).inBounds(pos.x)
  }

  case class Row(sections: Section*) {

    def inBounds(x: Int) = (sections zip offsets)
      .dropWhile { case (_, o) => o > x }
      .headOption.exists { case (s, o) => x - o < s.size }

    val start = sections.head.offset
    val end = sections.map(s => s.offset + s.size).sum

    val offsets = (Section(0) +: sections).sliding(2).map {
      case Seq(l, r) => l.size + r.offset
    }.scanLeft(0)(_+_).toSeq.tail
  }

  case class Section(size: Int = 1, offset: Int = 0)
}