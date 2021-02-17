package games.core

import games.core.{Piece, Vec}

sealed trait Action

object Action {

  case class Place[V <: Vec](pos: V, piece: Piece) extends Action

  case class Move[V <: Vec](from: V, to: V) extends Action {

    def doMove[S <: State.VState[V]](history: History[S]) =
      history.push(this, history.state.movePiece(from, to).asInstanceOf[S])

    def inBounds(manifold: Manifold[V]) =
      manifold.inBounds(to)

    def selfCapture(state: State.VState[V]) =
      state.pieces.get(to).exists(_.ownerId == state.turn)
  }

  case class Destroy[V <: Vec](pos: V) extends Action

  case class Select[V <: Vec]
    (pos: V, options: Seq[SelectOption])
  extends Action
  
  abstract class SelectOption(name: String, texture: String)
}