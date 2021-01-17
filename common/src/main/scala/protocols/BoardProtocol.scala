package protocols

import cats.syntax.functor._
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import models.{Board, Player, User, Participant}

object BoardProtocol {
  
  trait BoardRequest
  
  case class NewSpectator(boardId: String, id:0=0) extends BoardRequest
  case class JoinGame(boardId: String, userId: Int, id:1=1) extends BoardRequest
  case class RemovePlayer(boardId: String, playerId: Int, id:2=2) extends BoardRequest
  case class PromotePlayer(boardId: String, playerId: Int, id:3=3) extends BoardRequest
  case class DemotePlayer(boardId: String, playerId: Int, id:4=4) extends BoardRequest
  case class DeleteGame(boardId: String, id:5=5) extends BoardRequest
  case class StartGame(boardId: String, id:6=6) extends BoardRequest
  case class TakeAction(boardId: String, actionId: Int, id:7=7) extends BoardRequest

  implicit val encodeRequest: Encoder[BoardRequest] = Encoder.instance {
    case req: NewSpectator => req.asJson
    case req: JoinGame => req.asJson
    case req: RemovePlayer => req.asJson
    case req: PromotePlayer => req.asJson
    case req: DemotePlayer => req.asJson
    case req: DeleteGame => req.asJson
    case req: StartGame => req.asJson
    case req: TakeAction => req.asJson
  }

  implicit val decodeRequest: Decoder[BoardRequest] =
    List[Decoder[BoardRequest]] (
      Decoder[NewSpectator].widen,
      Decoder[JoinGame].widen,
      Decoder[RemovePlayer].widen,
      Decoder[PromotePlayer].widen,
      Decoder[DemotePlayer].widen,
      Decoder[DeleteGame].widen,
      Decoder[StartGame].widen,
      Decoder[TakeAction].widen
    ).reduceLeft(_ or _)

  trait BoardResponse

  case class SetBoard(board: Option[Board], id:0=0) extends BoardResponse
  case class SetPlayers(players: Seq[Participant], id:1=1) extends BoardResponse
  case class PushActions(actions: Seq[Int], id:2=2) extends BoardResponse

  implicit val encodeResponse: Encoder[BoardResponse] = Encoder.instance {
    case req: SetBoard => req.asJson
    case req: SetPlayers => req.asJson
    case req: PushActions => req.asJson
  }

  implicit val decodeResponse: Decoder[BoardResponse] =
    List[Decoder[BoardResponse]] (
      Decoder[SetBoard].widen,
      Decoder[SetPlayers].widen,
      Decoder[PushActions].widen
    ).reduceLeft(_ or _)
}