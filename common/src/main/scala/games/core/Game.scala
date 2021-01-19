package games.core

import games.core.States._
import games.core.Actions._
import games.core.Manifolds._
import games.core.Coordinates._
import games.core.Layouts._
import games.core.Backgrounds._

abstract class Game(val id: Int) {

  val name: String
  val players: Seq[Int]
  
  type Vec <: Coordinate
  type StateT <: AnyState
  
  val manifold: Manifold[Vec]
  val layout: Layout[Vec]
  val background: Background[Vec]

  val start: StateT
}