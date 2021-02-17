package games.core

trait Frontier extends State.Label

object Frontier {

  case class Place[P <: Piece](piece: Piece) extends Frontier
  case class Move[V <: Vec](from: V) extends Frontier

  object Moves {

    def update[V <: Vec, S <: State.VState[V]](state: S, pos: V): S = {

      val positions = pos +: canMoveTo(state, pos)

      positions.foldLeft(state) { (state, pos) =>
        state.pieces.get(pos) match {

          case Some(p: Piece.Moveable[_, _]) => {
            val piece = p.asInstanceOf[Piece.Moveable[V, S]]
            state
              .purgeLabel(Frontier.Move(pos))
              .addLabel(piece.moves(state, pos), Frontier.Move(pos))
              .asInstanceOf[S]
          }

          case _ => state
        }
      }
    }

    def updateAll[V <: Vec, S <: State[V, _ <: Piece, _]]
        (state: S, pos: Seq[V]): S = {
      pos.foldLeft(state)((s, p) => update(s, p))
    }

    def canMoveTo[V <: Vec, S <: State[V, _, _]]
        (state: S, pos: V): Seq[V] = {

      state.labels.get(pos).toSeq flatMap {
        case Frontier.Move(from) => Some(from.asInstanceOf[V])
        case _ => None
      }
    }

    def canMoveTo[V <: Vec, S <: State[V, _ <: Piece, _]]
        (state: S, pos: V, playerId: Int): Seq[V] = {

      canMoveTo(state, pos)
        .filter(p => state.pieces(p).ownerId == playerId)
    }
  }
}