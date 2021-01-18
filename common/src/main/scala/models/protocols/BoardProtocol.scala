package models.protocols

import models.{Board, Participant}

object BoardProtocol {
  
  sealed trait BoardRequest
  
  case class NewSpectator(boardId: String) extends BoardRequest
  case class JoinGame(boardId: String, userId: Int) extends BoardRequest
  case class RemovePlayer(boardId: String, playerId: Int) extends BoardRequest
  case class PromotePlayer(boardId: String, playerId: Int) extends BoardRequest
  case class DemotePlayer(boardId: String, playerId: Int) extends BoardRequest
  case class DeleteGame(boardId: String) extends BoardRequest
  case class StartGame(boardId: String) extends BoardRequest
  case class TakeAction(boardId: String, actionId: Int) extends BoardRequest

  sealed trait BoardResponse

  case class SetBoard(board: Option[Board]) extends BoardResponse
  case class SetPlayers(players: Seq[Participant]) extends BoardResponse
  case class PushActions(actions: Seq[Int]) extends BoardResponse

  sealed trait BoardFilter

  case object AllBoards extends BoardFilter
  case object FriendsBoards extends BoardFilter
  case object MyBoards extends BoardFilter
  case object MostRecent extends BoardFilter
}