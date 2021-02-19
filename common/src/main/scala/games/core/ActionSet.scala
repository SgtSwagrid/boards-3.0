package games.core

case class ActionSet[V <: Vec, P <: Piece, +A <: Action[V]] (
  private val actionSet: Iterable[(A, State[V, P])]
) {

  type S = State[V, P]

  def filterActions(p: A => Boolean): ActionSet[V, P, A] = {
    ActionSet(actionSet.filter { case a -> _ => p(a) })
  }

  def filterStates(p: S => Boolean): ActionSet[V, P, A] = {
    ActionSet(actionSet.filter { case _ -> s => p(s) })
  }

  def filter(p: (A, S) => Boolean): ActionSet[V, P, A] = {
    ActionSet(actionSet.filter { case a -> s => p(a, s) })
  }

  def mapActions[A2 <: Action[V]](f: A => A2): ActionSet[V, P, A2] = {
    ActionSet(actionSet.map { case a -> s => f(a) -> s })
  }

  def mapStates(f: S => S): ActionSet[V, P, A] = {
    ActionSet(actionSet.map { case a -> s => a -> f(s) })
  }

  def map(f: (A, S) => S): ActionSet[V, P, A] = {
    ActionSet(actionSet.map { case a -> s => a -> f(a, s) })
  }

  def orElse[A2 >: A <: Action[V]](other: ActionSet[V, P, A2]):
      ActionSet[V, P, A2] = {
    ActionSet((actionSet ++ other.actionSet).toMap)
  }

  def isEmpty: Boolean = actionSet.view.isEmpty

  def actions: Iterable[A] = {
    actionSet.map { case a -> _ => a }.toSet
  }

  def places: Iterable[Action.Place[V]] = {
    actions
      .filter(_.isInstanceOf[Action.Place[_]])
      .map(_.asInstanceOf[Action.Place[V]])
  }

  def moves: Iterable[Action.Move[V]] = {
    actions
      .filter(_.isInstanceOf[Action.Move[_]])
      .map(_.asInstanceOf[Action.Move[V]])
  }

  def successors: Iterable[S] = {
    actionSet.map { case a -> s =>
      s.copy(previous = Some(s), action = Some(a))
    }.toSet
  }
}

object ActionSet {

  def combine[V <: Vec, P <: Piece, A <: Action[V]]
      (sets: Iterable[ActionSet[V, P, A]]) = {
    
    sets.foldLeft(ActionSet[V, P, A](Nil))(_ orElse _)
  }

  def places[V <: Vec, P <: Piece](state: State[V, P], piece: P)
      (positions: Iterable[V]): ActionSet[V, P, Action.Place[V]] = {
    
    ActionSet(positions.view.map { pos =>
      Action.Place(pos, piece) -> state.addPiece(pos, piece)
    })
  }

  def moves[V <: Vec, P <: Piece](state: State[V, P], pos: V)
      (positions: Iterable[V]): ActionSet[V, P, Action.Move[V]] = {
    
    ActionSet(positions.view.map { to =>
      Action.Move(pos, to) -> state.movePiece(pos, to)
    })
  }

  def allMoves[V <: Vec, P <: Piece](state: State[V, P], player: Int):
      ActionSet[V, P, Action.Move[V]] = {
    
    val pieces = state.piecesByOwner.get(player).view
    val moveable = filterMoveable(state, pieces)
    
    combine(moveable.map { case pos -> piece =>
      piece.moves(state.copy(turn = player), pos)
    })
  }

  private def filterMoveable[V <: Vec, P <: Piece]
      (state: State[V, P], positions: Iterable[V]):
      Iterable[(V, Piece.Moveable[V, P])] = {

    positions.view.map(p => p -> state.pieces(p))
      .filter { case _ -> piece =>
        piece.isInstanceOf[Piece.Moveable[_, _]]
      }.map { case pos -> piece =>
        pos -> piece.asInstanceOf[Piece.Moveable[V, P]] 
      }
  }

  implicit class PlaceSet[V <: Vec, P <: Piece]
      (placeSet: ActionSet[V, P, Action.Place[V]]) {
    
    type A = Action.Place[V]

    def filterPos(p: V => Boolean): ActionSet[V, P, A] = {
      placeSet.filterActions(a => p(a.pos))
    }
  }

  implicit class MoveSet[V <: Vec, P <: Piece]
      (moveSet: ActionSet[V, P, Action.Move[V]]) {

    type A = Action.Move[V]

    def filterFrom(p: V => Boolean): ActionSet[V, P, A] = {
      moveSet.filterActions(a => p(a.from))
    }

    def filterTo(p: V => Boolean): ActionSet[V, P, A] = {
      moveSet.filterActions(a => p(a.to))
    }
    
    def attacking(pos: V): ActionSet[V, P, A] = {
      moveSet.filterActions(_.to == pos).asInstanceOf[ActionSet[V, P, A]]
    }
  }
}