package games.core

case class Player (
  id: Int,
  userId: Option[Int],
  username: String,
  turnOrder: Int,
  isOwner: Boolean,
  time: Int,
  resignationOffered: Boolean = false,
  drawOffered: Boolean = false
)