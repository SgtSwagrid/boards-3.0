package games

import games.core.{
  Background, Colour, Game, InputAction,
  Layout, Manifold, Piece, State, Vec2
}

class TicTacToe(val id: Int) extends Game {

  val name = "Tic Tac Toe"
  val players = Seq(2)

  sealed trait TicTacToePiece extends Piece

  type VecT = Vec2
  type StateT = State[VecT, TicTacToePiece, Null]
  
  val manifold = Manifold.Rectangle(3, 3)
  val layout = Layout.Grid
  val background = Background.Checkerboard(
    Colour.hintOfPensive, Colour.lynxWhite)

  def start(players: Int) = State()

  def successors(state: StateT) = Seq(state)
}