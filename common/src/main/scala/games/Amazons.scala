package games

import games.core.{
  Action, Background, Colour, Frontier, Game,
  Layout, Manifold, Piece, Pieces, State, Vec2
}

class Amazons(val id: Int) extends Game {

  val name = "Game of the Amazons"
  val players = Seq(2)
  val stages = 2

  val manifold = Manifold.Rectangle(10, 10)

  val background = Background.Checkerboard(
    Colour.brightYarrow, Colour.sourLemon)

  def layout(playerId: Option[Int]) = Layout.Grid

  sealed abstract class AmazonsPiece extends Piece

  case class Queen(ownerId: Int) extends AmazonsPiece {
    val colour = byOwner("white", "black")
    val texture: String = s"chess/${colour}_queen.png"
    
    def generateMoves(state: StateT, pos: Vec2) = {
      Vec2.cardinal.flatMap(manifold.rayUntil(pos, _, state.occupied))
    }
  }

  case class Arrow(ownerId: Int) extends AmazonsPiece {
    val colour = byOwner("white", "black")
    val texture: String = s"chess/${colour}_pawn.png"

    def generatePlaces(state: StateT) = {
      val lastQueenPos = state.action match {
          case Some(Action.Move(_, to: Vec2)) => to
          case _ => null
      }
        
      Vec2.cardinal.flatMap(manifold.rayUntil(lastQueenPos, _, state.occupied))
    }
  }

  def start(players: Int) = {
    new StateT()
    .withPlayers(2)
    .addPieces(Seq(Vec2(0, 3), Vec2(3, 0), Vec2(6, 0), Vec2(9, 3)), List.fill(4)(Queen(0)))
    .addPieces(Seq(Vec2(0, 6), Vec2(3, 9), Vec2(6, 9), Vec2(9, 6)), List.fill(4)(Queen(1)))
  }

  def next(state: StateT) = { 
    
    val moves = 
      if (state.stage == 0) {
        // Queens
        val queenLocs = state.occurences.get(Queen(state.turn))

        queenLocs.flatMap { from => {
            val tos = Queen(state.turn).generateMoves(state, from)

            val actions = tos.map{ to => 
              Action.Move(from, to)
            }
            
            actions.map { action =>  
              action -> state.movePiece(action.from, action.to)
            }.toMap
          }
        }
      } else {
        // Arrow after queen
        Arrow(state.turn).generatePlaces(state).map{ pos =>
          Action.Place(pos, Arrow(state.turn))
        }.map { action =>
          (action -> state.addPiece(action.pos, action.piece.asInstanceOf[PieceT]))
        }.toMap
      }

      moves.toMap.mapValues(state => {
        if (state.stage == 1) {
          val frontier = state.occurences.get(Queen(state.nextTurn())).flatMap(manifold.box(_, 1))

          if (frontier.exists(state.empty)) {
            state.endStageOrTurn(stages)
          } else {
            state.endGame(State.Winner(state.turn))
          }
        } else {
          state.endStageOrTurn(stages)
        }
      })
   }

  type VecT = Vec2
  type PieceT = AmazonsPiece
}
