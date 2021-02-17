package games

import games.core.{
  Action, Background, Colour, Frontier, Game,
  History, Layout, Manifold, Piece, State, Vec2
}

class Chess(val id: Int) extends Game {
  
  val name = "Chess"
  val players = Seq(2)

  sealed abstract class ChessPiece(name: String)
      extends Piece.Moveable[VecT, StateT] {

    val colour = byOwner("white", "black")
    val texture = s"chess/${colour}_$name.png"
  }

  case class Pawn(ownerId: Int) extends ChessPiece("pawn") {

    private val home = byOwner(1, 6)
    private val dir = byOwner(Vec2.N, Vec2.S)

    def moves(state: StateT, pos: Vec2) = {

      val forward = manifold
        .ray(pos, dir, if (pos.y == home) 2 else 1)
        .takeWhile(state.empty)

      val captures = Vec2.horz
        .map(pos + dir + _)
        .filter(state.enemy)

      (forward ++ captures)
    }
  }

  case class Rook(ownerId: Int) extends ChessPiece("rook") {
    def moves(state: StateT, pos: Vec2) = {
      Vec2.orthogonal.flatMap(manifold.rayTo(pos, _, state.occupied))
    }
  }

  case class Knight(ownerId: Int) extends ChessPiece("knight") {
    def moves(state: StateT, pos: Vec2) = {
      manifold.box(pos, 2).filter(manifold.taxiDist(pos, _) == 3)
    }
  }

  case class Bishop(ownerId: Int) extends ChessPiece("bishop") {
    def moves(state: StateT, pos: Vec2) = {
      Vec2.diagonal.flatMap(manifold.rayTo(pos, _, state.occupied))
    }
  }

  case class Queen(ownerId: Int) extends ChessPiece("queen") {
    def moves(state: StateT, pos: Vec2) = {
      Vec2.cardinal.flatMap(manifold.rayTo(pos, _, state.occupied))
    }
  }

  case class King(ownerId: Int) extends ChessPiece("king") {
    def moves(state: StateT, pos: Vec2) = {
      manifold.box(pos, 1)
    }
  }

  type VecT = Vec2
  type PieceT = ChessPiece
  type StateT = State[Vec2, ChessPiece, Null]

  val manifold = Manifold.Rectangle(8, 8)
  
  val background = Background.Checkerboard(
    Colour.sourLemon, Colour.brightYarrow)

  def layout(playerId: Option[Int]) =
    if (playerId.contains(1)) Layout.RotatedGrid
    else Layout.Grid

  def start(players: Int) = {

    def pieces(pid: Int) = Seq (
      Rook(pid), Knight(pid), Bishop(pid), King(pid),
      Queen(pid), Bishop(pid), Knight(pid), Rook(pid)
    )

    val state = new StateT()
      .withPlayers(2)
      .addPieces(manifold.row(0), pieces(0))
      .addPieces(manifold.row(7), pieces(1))
      .addPieces(manifold.row(1), List.fill(8)(Pawn(0)))
      .addPieces(manifold.row(6), List.fill(8)(Pawn(1)))

    Frontier.Moves.updateAll(state, manifold.positions)
  }

  def next(history: HistoryT): Iterable[HistoryT] = {

    val state = history.state

    moves(state)
      .map(m => history.push(m, state.pieces(m.from).doMove(state, m)))
      .filter(h => !check(h.state, state.turn))
  }

  private def moves(state: StateT) = {

    state.pieces.flatMap {
      case pos -> piece =>

        if (piece.ownerId == state.turn) {

          piece.moves(state, pos)
            .filter(manifold.inBounds)
            .filterNot(state.friendly)
            .map(Action.Move(pos, _))

        } else None
    }
  }

  private def check(state: StateT, playerId: Int): Boolean = {

    val kingPos = state.occurences.get(King(playerId)).head

    state.piecesByOwner.get((playerId + 1) % 2) exists { pos =>
      val piece = state.pieces(pos)
      piece.moves(state, pos).contains(kingPos)
    }
  }
}