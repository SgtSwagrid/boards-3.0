package games.core

trait Piece {
  val ownerId: Int
  val texture: String
  def byOwner[T](options: T*) = options(ownerId)
}

object Piece {

  trait Moveable[V <: Vec, S <: State[V, _ <: Piece, _]] extends Piece {

    final def moves(state: S, pos: V): Seq[(Action.Move[V], S)] = {

      generateMoves(state, pos)
        .map(Action.Move(pos, _))
        .filter(a => allowMove(state, a))
        .map(a => (state, applyMove(state, a), a))
        .filter { case (s1, s2, a) => validateMove(s1, s2, a) }
        .map { case (_, s2, a) => a -> s2 }
    }

    final def sight(state: S, pos: V): Seq[V] = {

      generateMoves(state, pos)
        .map(Action.Move(pos, _))
        .filter(a => allowMove(state, a))
        .map(_.to)
    }

    final def successors(history: History[S], pos: V): Seq[History[S]] = {
      moves(history.state, pos).map(history.push)
    }

    protected def generateMoves(state: S, pos: V): Seq[V]

    protected def allowMove(state: S, move: Action.Move[V]): Boolean = true

    protected def applyMove(state: S, move: Action.Move[V]): S = {
      
      if (endTurn(state, move)) state
        .movePiece(move.from, move.to)
        .endTurn()
        .asInstanceOf[S]

      else state
        .movePiece(move.from, move.to)
        .asInstanceOf[S]
    }

    protected def validateMove(before: S, after: S,
        action: Action.Move[V]): Boolean = true

    protected def endTurn(state: S, move: Action.Move[V]): Boolean = true
  }
}