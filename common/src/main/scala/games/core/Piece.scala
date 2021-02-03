package games.core

trait Piece {
  val ownerId: Int
  val texture: String
  def byOwner[T](options: T*) = options(ownerId)
}