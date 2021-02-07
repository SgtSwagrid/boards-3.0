package games

import games.core.{
  Action, Background, Colour, Game,
  Layout, Manifold, Piece, State, Vec2
}

class TicTacToe(val id: Int) extends Game {

  val name = "Tic Tac Toe"
  val players = Seq(2)

  sealed trait TicTacToePiece extends Piece

  type VecT = Vec2
  type PieceT = TicTacToePiece
  type StateT = State[VecT, TicTacToePiece, Null]
  
  val manifold = Manifold.Rectangle(3, 3)

  val background = Background.Checkerboard(
    Colour.hintOfPensive, Colour.lynxWhite)

  def layout(playerId: Option[Int]) = Layout.Grid

  def start(players: Int) = State()

  def next(history: HistoryT) = Seq(history)
}