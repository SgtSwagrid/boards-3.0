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
  type StateT <: State.VState[VecT]

  type Place = Action.Place[VecT]
  type Move = Action.Move[VecT]
  type Destroy = Action.Destroy[VecT]
  
  val manifold: Manifold[VecT]
  val layout: Layout[VecT]
  val background: Background[VecT]

  def start(players: Int): StateT
  
  def successors(state: StateT): Seq[StateT]

  def actions(state: StateT) =
    successors(state).flatMap(_.action)

  def takeAction(state: StateT, action: Action) =
    successors(state).find(_.action == Some(action))

  def validateAction(state: StateT, action: Action) =
    takeAction(state, action).isDefined

  def moves(state: StateT, pos: VecT): Seq[Move] =
    actions(state).filter {
      case Action.Move(from, to) => from == pos
      case _ => false
    }.map(_.asInstanceOf[Move])
}