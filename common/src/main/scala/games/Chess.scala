package games

import games.core.{
  Action, Background, Colour, Frontier, Game,
  Layout, Manifold, Piece, Pieces, State, Vec2
}

class Chess(val id: Int) extends Game {
  
  val name = "Chess"
  val players = Seq(2)

  val manifold = Manifold.Rectangle(8, 8)
  
  val background = Background.Checkerboard(
    Colour.brightYarrow, Colour.sourLemon)

  def layout(playerId: Option[Int]) =
    if (playerId.contains(1)) Layout.RotatedGrid
    else Layout.Grid

  sealed abstract class ChessPiece(name: String)
      extends Piece.Moveable[VecT, ChessPiece] {

    val colour = byOwner("white", "black")
    val texture = s"chess/${colour}_$name.png"

    override def allowMove(state: StateT, move: Action.Move[Vec2]) = {
      manifold.inBounds(move.to) && !state.friendly(move.to)
    }

    override def validateMove(before: StateT, after: StateT,
        move: Action.Move[Vec2]) = {

      val kingPos = after.occurences.get(King(before.turn)).head
      !Pieces.check(after, kingPos)
    }
  }

  case class Pawn(ownerId: Int) extends ChessPiece("pawn") {

    private val home = byOwner(1, 6)
    private val dir = byOwner(Vec2.N, Vec2.S)

    def generateMoves(state: StateT, pos: Vec2) = {

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
    def generateMoves(state: StateT, pos: Vec2) = {
      Vec2.orthogonal.flatMap(manifold.rayTo(pos, _, state.occupied))
    }
  }

  case class Knight(ownerId: Int) extends ChessPiece("knight") {
    def generateMoves(state: StateT, pos: Vec2) = {
      manifold.box(pos, 2).filter(manifold.taxiDist(pos, _) == 3)
    }
  }

  case class Bishop(ownerId: Int) extends ChessPiece("bishop") {
    def generateMoves(state: StateT, pos: Vec2) = {
      Vec2.diagonal.flatMap(manifold.rayTo(pos, _, state.occupied))
    }
  }

  case class Queen(ownerId: Int) extends ChessPiece("queen") {
    def generateMoves(state: StateT, pos: Vec2) = {
      Vec2.cardinal.flatMap(manifold.rayTo(pos, _, state.occupied))
    }
  }

  case class King(ownerId: Int) extends ChessPiece("king") {
    def generateMoves(state: StateT, pos: Vec2) = {
      manifold.box(pos, 1)
    }
  }

  def start(players: Int) = {

    def pieces(pid: Int) = Seq (
      Rook(pid), Knight(pid), Bishop(pid), Queen(pid),
      King(pid), Bishop(pid), Knight(pid), Rook(pid)
    )

    new StateT()
      .withPlayers(2)
      .addPieces(manifold.row(0), pieces(0))
      .addPieces(manifold.row(7), pieces(1))
      .addPieces(manifold.row(1), List.fill(8)(Pawn(0)))
      .addPieces(manifold.row(6), List.fill(8)(Pawn(1)))
  }

  def next(state: StateT) = {

    Pieces.moves(state, state.turn).toMap mapValues { state =>
      
      if (Pieces.mate(state, state.nextTurn())) {

        val kingPos = state.occurences.get(King(state.nextTurn())).head
        val outcome = if (Pieces.check(state, kingPos))
          State.Winner(state.turn) else State.Draw
        
        state.endGame(outcome)

      } else state.endTurn()
    }
  }

  type VecT = Vec2
  type PieceT = ChessPiece
}