package models

import java.time.LocalDateTime
import games.core.Manifest
import games.core.Scene
import games.core.Coordinates._
import games.core.States._
import games.core.Game

case class Board (
  id: String,
  gameId: Int,
  isPublic: Boolean = true,
  status: Int = 0,
  rematchBoardId: Option[String] = None,
  parentBoardId: Option[String] = None,
  modified: String = LocalDateTime.now().toString
) {
  def game = Manifest.Games(gameId)

  def setup = status == 0
  def ongoing = status == 1
  def ended = status == 2

  def scene[C <: Coordinate](state: CState[C]): Scene[_] = {
    new Scene(game.asInstanceOf[Game[C]], state)
  }
}