package games

import games.core.{
  Action, ActionSet, Background, Colour, Frontier,
  Game, Layout, Manifold, Piece, State, Vec2
}

import games.core.ActionSet.{PlaceSet, MoveSet}

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
      extends Piece.Moveable[Vec2, ChessPiece] {

    val colour = byOwner("white", "black")
    val texture = s"chess/${colour}_$name.png"
  }

  case class Pawn(ownerId: Int) extends ChessPiece("pawn") {

    private val home = byOwner(1, 6)
    private val dir = byOwner(Vec2.N, Vec2.S)

    def moves(state: StateT, pos: Vec2) = ActionSet.moves(state, pos) {

      val forward = manifold
        .ray(pos, dir, if (pos.y == home) 2 else 1)
        .takeWhile(state.empty)

      val captures = Vec2.horz
        .map(pos + dir + _)
        .filter(state.enemy(_))

      (forward ++ captures)
    }
  }

  case class Rook(ownerId: Int) extends ChessPiece("rook") {
    def moves(state: StateT, pos: Vec2) = ActionSet.moves(state, pos) {
      Vec2.orthogonal.flatMap(manifold.rayTo(pos, _, state.occupied))
    }
  }

  case class Knight(ownerId: Int) extends ChessPiece("knight") {
    def moves(state: StateT, pos: Vec2) = ActionSet.moves(state, pos) {
      manifold.box(pos, 2).filter(manifold.taxiDist(pos, _) == 3)
    }
  }

  case class Bishop(ownerId: Int) extends ChessPiece("bishop") {
    def moves(state: StateT, pos: Vec2) = ActionSet.moves(state, pos) {
      Vec2.diagonal.flatMap(manifold.rayTo(pos, _, state.occupied))
    }
  }

  case class Queen(ownerId: Int) extends ChessPiece("queen") {
    def moves(state: StateT, pos: Vec2) = ActionSet.moves(state, pos) {
      Vec2.cardinal.flatMap(manifold.rayTo(pos, _, state.occupied))
    }
  }

  case class King(ownerId: Int) extends ChessPiece("king") {
    def moves(state: StateT, pos: Vec2) = ActionSet.moves(state, pos) {
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
      .start
  }

  def actions(state: StateT) = {

    def moves(state: StateT, player: Int) = {
      ActionSet.allMoves(state, player)
        .filterTo(manifold.inBounds)
        .filterTo(!state.friendly(_, player))
    }

    def inCheck(state: StateT, player: Int) = {
      val kingPos = state.occurences.get(King(player)).head
      !moves(state, (player+1)%2).attacking(kingPos).isEmpty
    }

    val actions = moves(state, state.turn)
      .filterStates(!inCheck(_, state.turn))

    actions.map { (action, state) =>

      val mate = moves(state, state.nextTurn())
        .filterStates(!inCheck(_, state.nextTurn()))
        .isEmpty

      if (mate) {

        val check = inCheck(state, state.nextTurn())
        val outcome = if (check) State.Winner(state.turn) else State.Draw
        state.endGame(outcome)

      } else state.endTurn()
    }
  }

  type VecT = Vec2
  type PieceT = ChessPiece
}