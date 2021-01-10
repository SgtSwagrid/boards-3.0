package requests

import cats.syntax.functor._
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import models.{Board, Player, User, Participant}

object BoardRequests {
  
  trait BoardRequest
  
  case class NewSpectator(boardId: String, id: 0 = 0) extends BoardRequest
  case class JoinGame(boardId: String, userId: Int, id: 1 = 1) extends BoardRequest
  case class LeaveGame(boardId: String, userId: Int, id: 2 = 2) extends BoardRequest

  implicit val encodeRequest: Encoder[BoardRequest] = Encoder.instance {
    case req: NewSpectator => req.asJson
    case req: JoinGame => req.asJson
    case req: LeaveGame => req.asJson
  }

  implicit val decodeRequest: Decoder[BoardRequest] =
    List[Decoder[BoardRequest]] (
      Decoder[NewSpectator].widen,
      Decoder[JoinGame].widen,
      Decoder[LeaveGame].widen
    ).reduceLeft(_ or _)

  trait BoardResponse

  case class SetPlayers(players: Seq[Participant], id: 0 = 0) extends BoardResponse

  implicit val encodeResponse: Encoder[BoardResponse] = Encoder.instance {
    case req: SetPlayers => req.asJson
  }

  implicit val decodeResponse: Decoder[BoardResponse] =
    List[Decoder[BoardResponse]] (
      Decoder[SetPlayers].widen
    ).reduceLeft(_ or _)
}