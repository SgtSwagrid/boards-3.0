package models.protocols

import models.{Board, Player, User}

object BoardProtocol {
  
  sealed trait BoardRequest
  
  case object NewSpectator                  extends BoardRequest
  case class  AddPlayer     (userId: Int)   extends BoardRequest
  case class  RemovePlayer  (playerId: Int) extends BoardRequest
  case class  PromotePlayer (playerId: Int) extends BoardRequest
  case class  DemotePlayer  (playerId: Int) extends BoardRequest
  case object StartGame                     extends BoardRequest
  case object DeleteGame                    extends BoardRequest
  case object ResignGame                    extends BoardRequest
  case object DrawGame                      extends BoardRequest
  case object RematchGame                   extends BoardRequest
  case class  ForkGame      (ply: Int)      extends BoardRequest
  case class  TakeAction    (actionId: Int) extends BoardRequest

  sealed trait BoardResponse

  case class UpdateSession (
    board: Board,
    players: Seq[Player],
    users: Seq[User],
    rematch: Option[Board],
    forks: Seq[Board]
  ) extends BoardResponse

  case class PushActions(actions: Seq[ActionLog]) extends BoardResponse
  case class Redirect(board: Option[Board]) extends BoardResponse

  case class ActionLog(actionId: Int, turn: Int)

  sealed trait BoardFilter

  case object AllBoards extends BoardFilter
  case class UserBoards(userId: Int) extends BoardFilter
  case class FriendsBoards(userId: Int) extends BoardFilter
  case object MostRecent extends BoardFilter
}