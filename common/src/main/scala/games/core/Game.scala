package games.core

import games.core.{
  Action, Background, Colour,
  Layout, Manifold, State, Vec
}

abstract class Game {

  val id: Int
  val name: String
  val players: Seq[Int]
  
  type VecT <: Vec
  type PieceT <: Piece
  type StateT = State[VecT, PieceT]
  
  val manifold: Manifold[VecT]
  val background: Background[VecT]
  def layout(playerId: Option[Int]): Layout[VecT]

  def start(players: Int): StateT
  
  def next(state: StateT): Iterable[(Action, StateT)]

  def successors(state: StateT): Iterable[StateT] = {
    next(state).toSeq map { case (a, s) =>
      s.copy(previous = Some(state), action = Some(a))
    }
  }

  def actions(history: StateT): Iterable[Action] =
    successors(history).flatMap(_.action)

  def takeAction(history: StateT, action: Action): Option[StateT] =
    successors(history).find(_.action == Some(action))

  def validateAction(history: StateT, action: Action): Boolean =
    takeAction(history, action).isDefined
}