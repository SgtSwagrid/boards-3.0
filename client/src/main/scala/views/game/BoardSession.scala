package views.game

import org.scalajs.dom._
import models.{User, Player}

case class BoardSession (
  user: User,
  player: Option[Player],
  socket: WebSocket
) {
  def owner = player.exists(_.isOwner)
}