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
  type ActionT = Action[VecT]
  
  val manifold: Manifold[VecT]
  val background: Background[VecT]
  def layout(playerId: Option[Int]): Layout[VecT]

  def start(players: Int): StateT
  
  def actions(state: StateT): ActionSet[VecT, PieceT, ActionT]
}