package games.core

trait Piece {
  val ownerId: Int
  val texture: String
  def byOwner[T](options: T*) = options(ownerId)
}

object Piece {

  trait Moveable[V <: Vec, S <: State[V, _ <: Piece, _]] extends Piece {

    def moves(state: S, pos: V): Seq[V]

    def doMove(state: S, move: Action.Move[V]): S = {

      val newState = state
        .movePiece(move.from, move.to)
        .endTurn()
        .asInstanceOf[S]

      newState//Frontier.Moves.updateAll(newState, Seq(move.from, move.to))
    }
  }
}