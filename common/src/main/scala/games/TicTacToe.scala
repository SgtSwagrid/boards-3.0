package games

import games.core.{
  Action, Background, Colour, Frontier, Game,
  History, Layout, Manifold, Piece, State, Vec2
}

class TicTacToe(val id: Int) extends Game {

  val name = "Tic Tac Toe"
  val players = Seq(2)

  sealed abstract class TicTacToeBlank extends Piece {
    val colour = byOwner("X", "O")
    val texture: String = s"tictactoe/${colour}.png"
  }
  case class TicTacToePiece(ownerId: Int) extends TicTacToeBlank

  type VecT = Vec2
  type PieceT = TicTacToeBlank
  type StateT = State[VecT, TicTacToeBlank, Null]
  
  val manifold = Manifold.Rectangle(3, 3)

  val background = Background.Checkerboard(
    Colour.hintOfPensive, Colour.lynxWhite)

  def layout(playerId: Option[Int]) = Layout.Grid

  def start(players: Int) = new StateT().withPlayers(2)

  def next(history: HistoryT) = {
    
    val state = history.state

    val piece: TicTacToeBlank = TicTacToePiece(state.turn)

    val successors: Map[Place, StateT] = manifold.positions
     .filter(state.empty)
     .map(
        to => Action.Place(to, piece) -> state.addPiece(to, piece)
      ).toMap

    successors
      .mapValues(_.endTurn())
      .map(history.push)
  
  }
}