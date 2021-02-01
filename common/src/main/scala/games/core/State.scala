package games.core

import games.core.{InputAction, Piece, Vec}

case class State[V <: Vec, P <: Piece, S] (
  pieces: Map[V, P] = Map[V, P](),
  players: List[PlayerState[P]] = List[PlayerState[P]](),
  stage: S = null,
  previous: Option[State[V, P, S]] = None,
  action: Option[InputAction] = None,
  turn: Int = 0
) {

  def addPiece(pos: V, piece: P) = {
    copy(pieces = pieces + (pos -> piece))
  }

  def addPieces(pieces: Map[V, P]) = {
    copy(pieces = this.pieces ++ pieces)
  }

  def movePiece(from: V, to: V) = {
    copy(pieces = pieces - from + (to -> pieces(from)))
  }

  def removePiece(pos: V) = {
    copy(pieces = pieces - pos)
  }

  def pushAction(action: InputAction) = {
    copy(action = Some(action))
  }

  def endTurn(skip: Int = 1) = {
    copy(turn = (turn + skip) % players.size)
  }
}

case class PlayerState[P <: Piece](
  score: Int,
  captures: List[P]
)

object State {

  type AnyState = State[_ <: Vec, _ <: Piece, _]
  type VState[V <: Vec] = State[V, _ <: Piece, _]
}