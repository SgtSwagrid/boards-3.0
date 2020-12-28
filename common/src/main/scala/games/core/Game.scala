package games.core

import games.core.States._
import games.core.Rules._
import games.core.Actions._
import games.core.Manifolds._

abstract class Game(val id: Int) {
  
  type StateT <: AnyState

  val name: String
  val players: Seq[Int]
  
  val start: StateT
  val rules: Rule[StateT, Action]
}