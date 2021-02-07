package games

import games.core.{
  Action, Background, Colour, Game, History,
  Layout, Manifold, Piece, State, Vec2
}

class Chess(val id: Int) extends Game {
  
  val name = "Chess"
  val players = Seq(2)

  sealed abstract class ChessPiece(name: String) extends Piece {
    val colour = byOwner("white", "black")
    val texture = s"chess/${colour}_$name.png"
  }

  case class Pawn(ownerId: Int) extends ChessPiece("pawn")
  case class Rook(ownerId: Int) extends ChessPiece("rook")
  case class Knight(ownerId: Int) extends ChessPiece("knight")
  case class Bishop(ownerId: Int) extends ChessPiece("bishop")
  case class Queen(ownerId: Int) extends ChessPiece("queen")
  case class King(ownerId: Int) extends ChessPiece("king")

  type VecT = Vec2
  type PieceT = ChessPiece
  type StateT = State[Vec2, ChessPiece, Null]

  val manifold = Manifold.Rectangle(8, 8)
  val layout = Layout.Grid
  val background = Background.Checkerboard(
    Colour.sourLemon, Colour.brightYarrow)

  def start(players: Int) = {

    def pieces(pid: Int) = Seq (
      Rook(pid), Knight(pid), Bishop(pid), King(pid),
      Queen(pid), Bishop(pid), Knight(pid), Rook(pid)
    )

    new StateT()
      .withPlayers(2)
      .addPieces(manifold.row(0), pieces(0))
      .addPieces(manifold.row(7), pieces(1))
      .addPieces(manifold.row(1), List.fill(8)(Pawn(0)))
      .addPieces(manifold.row(6), List.fill(8)(Pawn(1)))
  }

  def next(history: HistoryT) = {

    val state = history.state

    val successors: Map[Move, StateT] = state.pieces.flatMap {

      case pos -> Pawn(state.turn) => {

        val home = pos.y == state.byPlayer(1, 6)
        val dir = state.byPlayer(Vec2.N, Vec2.S)

        val forward = Some(pos + dir)
          .filter(state.empty)

        val double = Option.when(home)(pos + (dir * 2))
          .filter(state.empty)

        val captures = Seq(Vec2.W, Vec2.E)
          .map(pos + dir + _).filter(state.enemy)

        (forward ++ double ++ captures)
          .filter(manifold.inBounds)
          .map(to => Action.Move(pos, to) -> state.movePiece(pos, to))
      }

      case pos -> Rook(state.turn) =>
        Vec2.orthogonal
          .flatMap(manifold.rayTo(pos, _, state.occupied))
          .map(to => Action.Move(pos, to) -> state.movePiece(pos, to))

      case pos -> Knight(state.turn) =>
        manifold.box(pos, 2)
          .filter(to => (to - pos).x.abs + (to - pos).y.abs == 3)
          .map(to => Action.Move(pos, to) -> state.movePiece(pos, to))

      case pos -> Bishop(state.turn) =>
        Vec2.diagonal
          .flatMap(manifold.rayTo(pos, _, state.occupied))
          .map(to => Action.Move(pos, to) -> state.movePiece(pos, to))

      case pos -> Queen(state.turn) =>
        Vec2.cardinal
          .flatMap(manifold.rayTo(pos, _, state.occupied))
          .map(to => Action.Move(pos, to) -> state.movePiece(pos, to))

      case pos -> King(state.turn) =>
        manifold.box(pos, 1)
          .map(to => Action.Move(pos, to) -> state.movePiece(pos, to))

      case _ => None
    } 

    successors
      .filterKeys(!_.selfCapture(state))
      .mapValues(_.endTurn())
      .map(history.push)
  }
}