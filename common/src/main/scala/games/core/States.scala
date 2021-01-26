package games.core

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
  ) {

    def addPiece(pos: C, piece: P): State[P, C, S] = {
      copy(pieces = pieces + (pos -> piece))
    }

    def addPieces(pieces: Map[C, P]): State[P, C, S] = {
      copy(pieces = this.pieces ++ pieces)
    }

    def movePiece(from: C, to: C): State[P, C, S] = {
      copy(pieces = pieces - from + (to -> pieces(from)))
    }

    def removePiece(pos: C): State[P, C, S] = {
      copy(pieces = pieces - pos)
    }
  }

  type AnyState = State[_ <: Piece, _ <: Coordinate, _]
  type CState[C <: Coordinate] = State[_ <: Piece, C, _]

  trait Piece {
    val ownerId: Int
    def texture: String
  }

  case class PlayerState[P <: Piece](
    score: Int,
    captures: List[P]
  )
}