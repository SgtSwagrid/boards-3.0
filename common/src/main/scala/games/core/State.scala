package games.core

import games.core.{Action, Piece, Vec}

case class State[V <: Vec, P <: Piece, S] (
  pieces: Map[V, P] = Map[V, P](),
  players: List[PlayerState[P]] = List[PlayerState[P]](),
  stage: S = null,
  turn: Int = 0
) {

  def pieceSeq = pieces.toSeq

  def addPiece(pos: V, piece: P) = {
    copy(pieces = pieces + (pos -> piece))
  }

  def addPieces(pieces: Iterable[(V, P)]) = {
    copy(pieces = this.pieces ++ pieces)
  }

  def addPieces(pos: Seq[V], pieces: Seq[P]) = {
    copy(pieces = this.pieces ++ (pos zip pieces))
  }

  def movePiece(from: V, to: V): State[V, P, S] = {
    copy(pieces = pieces - from + (to -> pieces(from)))
  }

  def removePiece(pos: V) = {
    copy(pieces = pieces - pos)
  }

  def endTurn(skip: Int = 1) = {
    copy(turn = (turn + skip) % players.size)
  }

  def withPlayers(numPlayers: Int) = {
    copy(players = List.fill(numPlayers)(PlayerState[P]()))
  }

  def byPlayer[T](options: T*) = options(turn)

  def empty(pos: V) = !pieces.isDefinedAt(pos)
  def occupied(pos: V) = pieces.isDefinedAt(pos)
  def friendly(pos: V) = pieces.get(pos).exists(_.ownerId == turn)
  def enemy(pos: V) = pieces.get(pos).exists(_.ownerId != turn)
}

case class PlayerState[P <: Piece] (
  score: Int = 0,
  captures: Seq[P] = Seq()
)

object State {

  type AnyState = State[_ <: Vec, _ <: Piece, _]
  type VState[V <: Vec] = State[V, _ <: Piece, _]
}