package games

import games.core.Game
import games.core.States._
import games.core.Pieces._
import games.core.Rules._
import games.core.Actions._
import games.core.Manifolds._
import games.core.Coordinates._

class TicTacToe(id: Int) extends Game(id) {

  val name = "Tic Tac Toe"
  val players = Seq(2)

  sealed trait TicTacToePiece extends Piece

  type StateT = State[TicTacToePiece, Vec2, Null]
  
  val manifold = GridManifold(3, 3)

  val start = State()
  val rules: Rule[StateT, Action] = null
}