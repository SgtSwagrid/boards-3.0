package games.core

object Pieces {

  def moveable[V <: Vec, P <: Piece](state: State[V, P], positions: Iterable[V]):
      Iterable[(V, Piece.Moveable[V, P])] = {

    positions.view.map(p => p -> state.pieces(p))
      .filter { case _ -> piece => piece.isInstanceOf[Piece.Moveable[_, _]] }
      .map { case pos -> piece => pos ->
        piece.asInstanceOf[Piece.Moveable[V, P]] 
      }
  }

  def moves[V <: Vec, P <: Piece](state: State[V, P], playerId: Int):
      Iterable[(Action.Move[V], State[V, P])] = {

    moveable(state, state.piecesByOwner.get(playerId))
      .flatMap { case pos -> piece =>
        piece.moves(state.copy(turn = playerId)
          .asInstanceOf[State[V, P]], pos)
      }
  }

  def check[V <: Vec, P <: Piece]
      (state: State[V, P], pos: V): Boolean = {

    val attackers = (0 until state.players.size)
      .filter(_ != state.pieces(pos).ownerId)
      .flatMap(state.piecesByOwner.get)

    moveable(state, attackers) exists { case from -> piece =>
      val newState = state.copy(turn = piece.ownerId).asInstanceOf[State[V, P]]
      piece.sight(newState, from).contains(pos)
    }
  }

  def mate[V <: Vec, P <: Piece]
      (state: State[V, P], playerId: Int): Boolean = {
    
    moves(state, playerId).isEmpty
  }
}