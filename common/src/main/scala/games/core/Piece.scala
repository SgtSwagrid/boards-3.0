package games.core

trait Piece {
  val ownerId: Int
  val texture: String
  def byOwner[T](options: T*) = options(ownerId)
}

object Piece {

  trait Moveable[V <: Vec, P <: Piece] extends Piece {

    final def moves(state: State[V, P], pos: V):
        Seq[(Action.Move[V], State[V, P])] = {

      generateMoves(state, pos)
        .map(Action.Move(pos, _))
        .filter(a => allowMove(state, a))
        .map(a => (state, applyMove(state, a), a))
        .filter { case (s1, s2, a) => validateMove(s1, s2, a) }
        .map { case (_, s2, a) => a -> s2 }
    }

    final def sight(state: State[V, P], pos: V): Seq[V] = {

      generateMoves(state, pos)
        .map(Action.Move(pos, _))
        .filter(a => allowMove(state, a))
        .map(_.to)
    }

    protected def generateMoves(state: State[V, P], pos: V): Seq[V]

    protected def allowMove(state: State[V, P], move: Action.Move[V]):
        Boolean = true

    protected def applyMove(state: State[V, P], move: Action.Move[V]): 
        State[V, P] = {
      
      state
        .movePiece(move.from, move.to)
        .asInstanceOf[State[V, P]]
    }

    protected def validateMove(before: State[V, P], after: State[V, P],
        action: Action.Move[V]): Boolean = true
  }
}