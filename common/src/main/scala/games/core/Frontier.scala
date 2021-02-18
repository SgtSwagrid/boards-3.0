package games.core

trait Frontier extends State.Label

object Frontier {

  case class Place[P <: Piece](piece: Piece) extends Frontier
  case class Move[V <: Vec](from: V) extends Frontier

  object Moves {

    def update[V <: Vec, P <: Piece]
        (state: State[V, P], pos: V): State[V, P] = {

      val positions = pos +: canMoveTo(state, pos)

      positions.foldLeft(state) { (state, pos) =>
        state.pieces.get(pos) match {

          case Some(p: Piece.Moveable[_, _]) => {
            val piece = p.asInstanceOf[Piece.Moveable[V, P]]
            state
              .purgeLabel(Frontier.Move(pos))
              .addLabels (
                piece.moves(state, pos)
                  .map { case a -> _ => a.to },
                Frontier.Move(pos)
              )
              .asInstanceOf[State[V, P]]
          }

          case _ => state
        }
      }
    }

    def updateAll[V <: Vec, P <: Piece]
        (state: State[V, P], pos: Seq[V]): State[V, P] = {
      pos.foldLeft(state)((s, p) => update(s, p))
    }

    def canMoveTo[V <: Vec, P <: Piece]
        (state: State[V, P], pos: V): Seq[V] = {

      state.labels.get(pos).toSeq flatMap {
        case Frontier.Move(from) => Some(from.asInstanceOf[V])
        case _ => None
      }
    }

    def canMoveTo[V <: Vec, P <: Piece]
        (state: State[V, P], pos: V, playerId: Int): Seq[V] = {

      canMoveTo(state, pos)
        .filter(p => state.pieces(p).ownerId == playerId)
    }
  }
}