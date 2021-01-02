package games.core

import play.api.libs.json._

object GameInstance {

  case class Board (
    id: String,
    gameId: Int,
    isPublic: Boolean,
    status: Int,
    players: Seq[Player],
    actions: Seq[Int],
    rematchBoardId: Option[String],
    parentBoardId: Option[String]
  ) {
    def game = Manifest.Games(gameId)
  }

  case class Player (
    id: Int,
    userId: Option[Int],
    username: String,
    turnOrder: Int,
    isOwner: Boolean,
    time: Int,
    resignOffer: Boolean = false,
    drawOffer: Boolean = false,
    undoOffer: Boolean = false
  )

  implicit val PlayerFormat = Json.format[Player]
  implicit val BoardFormat = Json.format[Board]
}