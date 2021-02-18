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
  type StateT <: State[VecT, PieceT, _]
  type PieceT <: Piece
  type HistoryT = History[StateT]

  type Place = Action.Place[VecT]
  type Move = Action.Move[VecT]
  type Destroy = Action.Destroy[VecT]
  
  val manifold: Manifold[VecT]
  val background: Background[VecT]
  def layout(playerId: Option[Int]): Layout[VecT]

  def start(players: Int): StateT
  
  def next(history: HistoryT): Iterable[(Action, StateT)]

  def successors(history: HistoryT): Iterable[HistoryT] =
    next(history).map(history.push)

  def actions(history: HistoryT): Iterable[Action] =
    successors(history).flatMap(_.action)

  def takeAction(history: HistoryT, action: Action): Option[HistoryT] =
    successors(history).find(_.action == Some(action))

  def validateAction(history: HistoryT, action: Action): Boolean =
    takeAction(history, action).isDefined
}