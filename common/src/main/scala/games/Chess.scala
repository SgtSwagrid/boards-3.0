package games

import games.core.{
  Background, Colour, Game, InputAction,
  Layout, Manifold, Piece, State, Vec2
}

class Chess(val id: Int) extends Game {
  
  val name = "Chess"
  val players = Seq(2)

  sealed abstract class ChessPiece(name: String) extends Piece {
    val colour = Seq("white", "black")(ownerId)
    val texture = s"chess/${colour}_$name.png"
  }

  case class Pawn(ownerId: Int) extends ChessPiece("pawn")
  case class Rook(ownerId: Int) extends ChessPiece("rook")
  case class Knight(ownerId: Int) extends ChessPiece("knight")
  case class Bishop(ownerId: Int) extends ChessPiece("bishop")
  case class Queen(ownerId: Int) extends ChessPiece("queen")
  case class King(ownerId: Int) extends ChessPiece("king")

  type VecT = Vec2
  type StateT = State[Vec2, ChessPiece, Null]

  val manifold = Manifold.Rectangle(8, 8)
  val layout = Layout.Grid
  val background = Background.Checkerboard(
    Colour.sourLemon, Colour.brightYarrow)

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

  def successors(state: StateT) = {

    state.pieces.filter(_._2.isInstanceOf[Pawn]).map {
      case (pos, pawn) =>
        state.movePiece(pos, pos + Vec2.up)
          .pushAction(InputAction.Move(pos, pos + Vec2.up))
    }.toSeq
  }
}