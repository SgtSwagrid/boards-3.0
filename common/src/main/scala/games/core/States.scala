package games.core

import games.core.Pieces._
import games.core.Actions._
import games.core.Manifolds._
import games.core.Coordinates._

object States {

  case class State[P <: Piece, C <: Coordinate, S] (
    pieces: Map[C, P] = Map[C, P](),
    players: List[PlayerState[P]] = List[PlayerState[P]](),
    stage: S = null,
    previous: Option[State[P, C, S]] = None,
    action: Option[Action] = None
  )

  type AnyState = State[_ <: Piece, _ <: Coordinate, _]
  type CState[C <: Coordinate] = State[_ <: Piece, C, _]

  case class PlayerState[P <: Piece](
    score: Int,
    captures: List[P]
  )
}