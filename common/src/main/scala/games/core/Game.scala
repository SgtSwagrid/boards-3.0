package games.core

import games.core.States._
import games.core.Actions._
import games.core.Manifolds._
import games.core.Coordinates._
import games.core.Layouts._
import games.core.Backgrounds._

abstract class Game[C <: Coordinate](val id: Int) {

  val name: String
  val players: Seq[Int]
  
  type Vec = C
  type StateT <: State[_ <: Piece, C, _]
  
  val manifold: Manifold[C]
  val layout: Layout[C]
  val background: Background[C]

  def start(players: Int): StateT
}

object Game {
  type AnyGame = Game[_ <: Coordinate]
}