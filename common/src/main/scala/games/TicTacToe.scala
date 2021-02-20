package games

import games.core.{
  Action, Background, Colour, Frontier, Game,
  Layout, Manifold, Piece, Pieces, State, Vec2
}

class TicTacToe(val id: Int) extends Game {

  val name = "Tic Tac Toe"
  val streak = 3
  val players = Seq(2)

  val manifold = Manifold.Rectangle(3, 3)

  val background = Background.Checkerboard(
    Colour.hintOfPensive, Colour.lynxWhite)

  def layout(playerId: Option[Int]) = Layout.Grid

  case class TicTacToePiece(ownerId: Int) extends Piece {
    val player = byOwner("X", "O")
    val texture = s"tictactoe/${player}.png"
  }

  def start(players: Int) = new StateT().withPlayers(2)

  def next(state: StateT) = {

    val piece = TicTacToePiece(state.turn)

    manifold.positions
      .filter(state.empty)
      .map(pos => Action.Place(pos, piece) -> state.addPiece(pos, piece))
      .toMap
      .map{ case ((action@Action.Place(pos, _)) -> state) => (action ->
        (if (streakFormed(state, pos, streak)) {
          state.endGame(State.Winner(state.turn))
        } else if (state.pieces.size == manifold.size) {
          state.endGame(State.Draw)
        } else {
          state.endTurn()
        }))
      }
  }

  /** Returns the Outcone of the state there is one */
  def streakFormed(state: StateT, pos: Vec2, streak: Int = 3): Boolean = {

    val directions: Seq[Vec2] = Vec2.halfCardinal

    val streaks: Seq[Seq[Vec2]] = directions
      .flatMap(manifold.linesThrough(pos, _, streak))
      .filter(_.size == streak)

    streaks.exists(_.forall(state.friendly))
  }

  type VecT = Vec2
  type PieceT = TicTacToePiece
}