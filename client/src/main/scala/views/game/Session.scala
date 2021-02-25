package views.game

import scala.collection.decorators._
import scala.collection.immutable.MultiDict

import org.scalajs.dom.WebSocket

import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

import models.{Board, Player, User}
import games.core.{Action, ActionSet, Piece, State, Vec}
import models.protocols.BoardProtocol._

case class Session[V <: Vec, P <: Piece] (
  user: User,
  socket: WebSocket,
  board: Board,
  players: Seq[Player],
  users: Seq[User],
  currentState: State[V, P],
  visibleState: State[V, P],
  rematch: Option[Board],
  forks: Seq[Board]
) {
  
  val game = board.game

  val player = (players zip users)
    .find { case (_, u) => u.id == user.id }
    .map { case (p, _) => p }

  val isOwner = player.exists(_.isOwner)

  val (resignedPlayers, resignedUsers) = (players zip users)
    .filter { case (player, _) => player.resign }
    .unzip

  val (drawnPlayers, drawnUsers) = (players zip users)
    .filter { case (player, _) => player.draw }
    .unzip

  val currentOutcome = {

    if (board.status == 0)
      visibleState.outcome

    else if (resignedPlayers.size == players.size-1 && players.size != 0)
      State.Winner(players.find(!_.resign).get.turnOrder)

    else if (drawnPlayers.size == players.size && players.size != 0)
      State.Draw
      
    else currentState.outcome
  }

  val visibleOutcome = {
    if (visibleState == currentState) currentOutcome
    else visibleState.outcome
  }

  val status = {
    if (board.status == 0) Session.Setup
    else if (currentOutcome == State.Ongoing) Session.Playing
    else Session.Ended
  }

  val current = visibleState == currentState
  val ongoing = status == Session.Playing
  val myTurn = player.exists(_.turnOrder == visibleState.turn)
  val canPlay = current && ongoing && myTurn

  val actionSet = game.actions(currentState.asInstanceOf[game.StateT])
    .asInstanceOf[ActionSet[V, P, Action[V]]]

  val actions = actionSet.actions.toSet
  val places = actionSet.places.toSet
  val moves = actionSet.moves.toSet
  val placesAt = places.map(a => a.pos -> a).to(MultiDict)
  val movesFrom = moves.map(a => a.from -> a).to(MultiDict)

  val successors: Seq[State[V, P]] = actionSet.successors.toSeq
  val sortedActions = successors.flatMap(_.action)

  def setBoard(board: Board) = {

    val state = if (this.board.status == 0 && board.status > 0)
      game.start(players.size).asInstanceOf[State[V, P]]
      else currentState

    copy(board = board, currentState = state, visibleState = state)
  }

  def setPlayers(players: Seq[Player], users: Seq[User]) = {
    copy(players = players, users = users)
  }

  def takeAction(actionId: Int, playerId: Int) = {

    if (successors.isDefinedAt(actionId)
        && currentState.turn == playerId) {
          
      val state = successors(actionId)
      copy(currentState = state, visibleState = state)

    } else this
  }

  def startGame() = {
    val action: BoardRequest = StartGame
    socket.send(action.asJson.toString)
  }

  def deleteGame() = {
    val action: BoardRequest = DeleteGame
    socket.send(action.asJson.toString)
  }

  def joinGame() = {
    val action: BoardRequest = AddPlayer(user.id)
    socket.send(action.asJson.toString)
  }

  def leaveGame() = {
    val action: BoardRequest = RemovePlayer(player.get.id)
    socket.send(action.asJson.toString)
  }

  def removePlayer(player: Player) = {
    val action: BoardRequest = RemovePlayer(player.id)
    socket.send(action.asJson.toString)
  }

  def promotePlayer(player: Player) = {
    val action: BoardRequest = PromotePlayer(player.id)
    socket.send(action.asJson.toString)
  }

  def demotePlayer(player: Player) = {
    val action: BoardRequest = DemotePlayer(player.id)
    socket.send(action.asJson.toString)
  }

  def resignGame() = {
    val action: BoardRequest = ResignGame
    socket.send(action.asJson.toString)
  }

  def drawGame() = {
    val action: BoardRequest = DrawGame
    socket.send(action.asJson.toString)
  }

  def rematchGame() = {
    val action: BoardRequest = RematchGame
    socket.send(action.asJson.toString)
  }

  def forkGame() = {
    val action: BoardRequest = ForkGame(visibleState.ply)
    socket.send(action.asJson.toString)
  }

  def takeAction(action: Action[V]) {

    val actionId = sortedActions.indexOf(action)
    
    if (actionId != -1) {
      val request: BoardRequest = TakeAction(actionId)
      socket.send(request.asJson.toString)
    }
  }

  def goto(state: State.AnyState) = {
    copy(visibleState = state.asInstanceOf[State[V, P]])
  }

  def gotoFirst = {
    goto(visibleState.history.last)
  }

  def gotoPrevious = {
    goto(visibleState.previous.getOrElse(visibleState))
  }

  def gotoNext = {
    goto(currentState.history
      .find(_.previous.contains(visibleState))
      .getOrElse(visibleState))
  }

  def gotoLast = {
    goto(currentState)
  }
}

object Session {

  type AnySession = Session[_ <: Vec, _ <: Piece]

  sealed trait Status
  case object Setup extends Status
  case object Playing extends Status
  case object Ended extends Status
}