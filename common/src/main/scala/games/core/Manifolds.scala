package games.core

import games.core.Coordinates._

object Manifolds {

  trait Manifold[C <: Coordinate] {
    val positions: Seq[C]
    def inBounds(pos: C): Boolean
  }

  case class GridManifold(width: Int, height: Int) extends Manifold[Vec2] {
    val positions = for(x <- 0 until width; y <- 0 until height) yield Vec2(x, y)
    def inBounds(pos: Vec2) = pos.x >= 0 && pos.y < width && pos.y >= 0 && pos.y < height
  }

  abstract class RowManifold(rows: Row*) extends Manifold[Vec2] {
    protected val start = rows.map(_.offset).min
    protected val end = rows.map(r => r.offset + r.size).max
    val width = start - end + 1
    val height = rows.size
  }

  case class LeftRowManifold(rows: Row*) extends RowManifold(rows: _*) {

    val positions = rows.zipWithIndex flatMap {
      case (row, y) => (0 until row.size) map {
        x => Vec2(x + (row.offset - start), y)
      }
    }

    def inBounds(pos: Vec2) = {
      pos.y >= 0 && pos.y < height &&
        pos.y >= (rows(pos.y).offset - start) &&
        pos.y < (rows(pos.y).offset - start) + rows(pos.y).size
    }
  }

  //case class RightRowManifold(rows: Row*) extends RowManifold(rows: _*)

  //case class CentreRowManifold(rows: Row*) extends RowManifold(rows: _*)

  case class Row(size: Int, offset: Int)

  

  sealed trait Part
  case class Group(size: Int) extends Part
  case class Space(size: Int) extends Part
}