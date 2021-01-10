package models

import games.core.Manifest

case class Board (
  id: String,
  gameId: Int,
  isPublic: Boolean = true,
  status: Int = 0,
  rematchBoardId: Option[String] = None,
  parentBoardId: Option[String] = None
) {
  def game = Manifest.Games(gameId)
}