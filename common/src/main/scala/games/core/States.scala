package games.core

import games.core.Pieces._
import games.core.Manifolds._
import games.core.Coordinates._

object States {

  case class State[P <: Piece, C <: Coordinate, S] (
    pieces: Map[C, P] = Map[C, P](),
    players: List[PlayerState[P]] = List[PlayerState[P]](),
    stage: S = null
  )

  type AnyState = State[_ <: Piece, _ <: Coordinate, _]

  case class PlayerState[P <: Piece](
    score: Int,
    captures: List[P]
  )
}