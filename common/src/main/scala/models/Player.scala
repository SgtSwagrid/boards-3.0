package models

case class Player (
  id: Int = -1,
  userId: Int,
  boardId: String,
  turnOrder: Int,
  isOwner: Boolean = false,
  time: Int = 0,
  resign: Boolean = false,
  draw: Boolean = false,
  revert: Option[Int] = None
)