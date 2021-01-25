package games.core

object Pieces {
  
  trait PieceType {
    val texture: String
  }

  trait Piece {
    val pieceType: PieceType
  }
}
