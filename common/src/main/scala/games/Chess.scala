package games

import games.core.Game
import games.core.States._
import games.core.Actions._
import games.core.Manifolds._
import games.core.Coordinates._
import games.core.Layouts._
import games.core.Backgrounds._
import games.core.Colour

class Chess(id: Int) extends Game[Vec2](id) {
  
  val name = "Chess"
  val players = Seq(2)

  sealed abstract class ChessPiece(name: String) extends Piece {
    def colour = Seq("white", "black")(ownerId)
    def texture = s"chess/${colour}_$name.png"
  }

  case class Pawn(ownerId: Int) extends ChessPiece("pawn")
  case class Rook(ownerId: Int) extends ChessPiece("rook")
  case class Knight(ownerId: Int) extends ChessPiece("knight")
  case class Bishop(ownerId: Int) extends ChessPiece("bishop")
  case class Queen(ownerId: Int) extends ChessPiece("queen")
  case class King(ownerId: Int) extends ChessPiece("king")

  type StateT = State[ChessPiece, Vec, Null]

  val manifold = RectangleManifold(8, 8)
  val layout = GridLayout
  val background = Checkerboard(Colour.sourLemon, Colour.brightYarrow)

  def start(players: Int) = {

    def home(x: Int, pid: Int) = Seq (
      Rook(pid), Knight(pid), Bishop(pid), King(pid),
      Queen(pid), Bishop(pid), Knight(pid), Rook(pid)
    )(x)

    val pieces = for {
      (pid, y) <- Seq((0, 0), (1, 7))
      pos <- manifold.row(y)
    } yield pos -> home(pos.x, pid)

    val pawns = for {
      (pid, y) <- Seq((0, 1), (1, 6))
      pos <- manifold.row(y)
    } yield pos -> Pawn(pid)

    State((pieces ++ pawns).toMap)
  }
}