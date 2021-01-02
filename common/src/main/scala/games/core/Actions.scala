package games.core

import games.core.Coordinates._

object Actions {

  sealed trait Action
  
  case class Place[C <: Coordinate](at: C) extends Action
  case class Move[C <: Coordinate](from: C, to: C) extends Action
  case class Destroy[C <: Coordinate](at: C) extends Action

  case class Select[C <: Coordinate, S <: Selection](at: C, selection: S) extends Action
  abstract class Selection(val name: String, val image: String)
}