package models

case class Player (
  id: Int = -1,
  userId: Int,
  boardId: String,
  turnOrder: Int,
  isOwner: Boolean = false,
  time: Int = 0,
  resignOffer: Boolean = false,
  drawOffer: Boolean = false,
  undoOffer: Boolean = false
)