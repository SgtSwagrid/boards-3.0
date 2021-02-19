package games

import games.core.{
  Action, ActionSet, Background, Colour, Frontier,
  Game, Layout, Manifold, Piece, State, Vec2
}

class Amazons(val id: Int) extends Game {

  val name = "Game of the Amazons"
  val players = Seq(2)

  val manifold = Manifold.Rectangle(10, 10)

  val background = Background.Checkerboard(
    Colour.brightYarrow, Colour.sourLemon)

  def layout(playerId: Option[Int]) = Layout.Grid

  sealed abstract class AmazonsPiece(name: String) extends Piece {
    val colour = byOwner("white", "black")
    val texture: String = s"chess/${colour}_$name.png"
  }

  case class Queen(ownerId: Int) extends AmazonsPiece("queen")
  case class Arrow(ownerId: Int) extends AmazonsPiece("pawn")

  def start(players: Int) = {
    new StateT()
    .withPlayers(2)
    .addPieces(Seq(Vec2(0, 3), Vec2(3, 0), Vec2(6, 0), Vec2(9, 3)), List.fill(4)(Queen(0)))
    .addPieces(Seq(Vec2(0, 6), Vec2(3, 9), Vec2(6, 9), Vec2(9, 6)), List.fill(4)(Queen(1)))
  }

  def actions(state: StateT) = {

    println(state)

    if (state.stage == 0) {

      val queens = state.occurences.get(Queen(state.turn))

      ActionSet.combine(queens.map(pos => ActionSet.moves(state, pos) {
        Vec2.cardinal.flatMap(manifold.rayUntil(pos, _, state.occupied))
      })).mapStates(_.endStage())

    } else {

      val action = state.action.get.asInstanceOf[Action.Move[Vec2]]
      val queen = state.pieces(action.to)

      ActionSet.places(state, Arrow(state.turn)) {
        Vec2.cardinal.flatMap(manifold.rayUntil(action.to, _, state.occupied))
      }.mapStates(_.endTurn())
    }
  }

  type VecT = Vec2
  type PieceT = AmazonsPiece
}