package games.core

import games.core.States._
import games.core.Pieces._
import games.core.Coordinates._

object Kernels {
  
  trait Kernel[C <: Coordinate] {
    def positions: Seq[C]
    def pieces[P <: Piece](state: State[P, C, _]): Seq[P]
  }

  class BoxKernel
}
