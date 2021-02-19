package games

import games.core.{
  Action, ActionSet, Background, Colour, Frontier,
  Game, Layout, Manifold, Piece, State, Vec2
}

import games.core.ActionSet.{PlaceSet, MoveSet}

class TicTacToe(val id: Int) extends Game {

  val name = "Tic Tac Toe"
  val players = Seq(2)

  val manifold = Manifold.Rectangle(3, 3)

  val background = Background.Checkerboard(
    Colour.hintOfPensive, Colour.lynxWhite)

  def layout(playerId: Option[Int]) = Layout.Grid

  case class TicTacToePiece(ownerId: Int) extends Piece {
    val player = byOwner("X", "O")
    val texture = s"tictactoe/${player}.png"
  }

  def start(players: Int) = new StateT().withPlayers(2).start

  def actions(state: StateT) = {

    ActionSet.places(state, TicTacToePiece(state.turn)) {
      manifold.positions.filter(state.empty)
    }.mapStates(_.endTurn())
  }

  type VecT = Vec2
  type PieceT = TicTacToePiece
}