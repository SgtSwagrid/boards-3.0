package models

import scala.math._
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.{ExecutionContext, Future}
import models.schema.BoardSchema._
import models.schema.PlayerSchema._
import models.schema.ActionSchema._
import models.schema.UserSchema._
import models.{Board, Player, User}
import games.core.Manifest.Games
import slick.dbio.DBIOAction
import java.time.LocalDateTime
import models.protocols.SearchProtocol._
import models.protocols.BoardProtocol._

class BoardModel(db: Database)(implicit ec: ExecutionContext) {

  type BoardsQuery = Query[Boards, Board, Seq]

  private val users = new UserModel(db)
  private val search = new SearchModel(db)

  def createBoard(gameId: Int, userId: Int): Future[Board] = {

    val boardId = randomId()
    val board = Board(id=boardId, gameId=gameId)
    val player = Player(userId=userId, boardId=boardId, turnOrder=0, isOwner=true)

    val create = for {
      _ <- Boards += board
      _ <- Players += player
      Some(board) <- DBAction.getBoardById(boardId)
    } yield board

    db.run(create)
  }

  def joinBoard(boardId: String, userId: Int): Future[Option[Player]] = {

    val join = for {

      Some(board) <- DBAction.getBoardById(boardId)
      players <- DBAction.getPlayersByBoard(boardId)

      player <- if (canJoin(board, players, userId)) 
        DBAction.addPlayer(boardId, userId, players.size)
      else DBIO.successful(None)

    } yield player

    db.run(join)
  }

  def removePlayer(boardId: String, playerId: Int): Future[Boolean] = {

    val remove = for {

      Some(player) <- DBAction.getPlayerById(playerId)
      removed <- Players.filter(_.id === playerId).delete.map(_ > 0)
      players <- DBAction.getPlayersByBoard(boardId)
      
      _ <- DBIOAction.sequence(players
        .filter(_.turnOrder > player.turnOrder)
        .map(player => DBQuery.playerById(player.id)
          .map(_.turnOrder).update(player.turnOrder-1)))

      _ <- if (players.size == 0) DBAction.deleteBoard(boardId)
        else if (player.isOwner) DBAction.transferBoard(boardId, players.head.id)
        else DBIOAction.successful(0)

      _ <- DBAction.touchBoard(boardId)

    } yield removed

    db.run(remove)
  }

  def promotePlayer(boardId: String, playerId: Int): Future[Unit] = {

    val promote = for {
      Some(player) <- DBAction.getPlayerById(playerId)
      Some(previous) <- DBAction.getPlayerByPosition(boardId, player.turnOrder-1)
      _ <- DBAction.swapPlayers(player, previous)
      _ <- DBAction.touchBoard(boardId)
    } yield ()

    db.run(promote)
  }

  def demotePlayer(boardId: String, playerId: Int): Future[Unit] = {

    val demote = for {
      Some(player) <- DBAction.getPlayerById(playerId)
      Some(next) <- DBAction.getPlayerByPosition(boardId, player.turnOrder+1)
      _ <- DBAction.swapPlayers(player, next)
      _ <- DBAction.touchBoard(boardId)
    } yield ()

    db.run(demote)
  }

  def startGame(boardId: String): Future[Boolean] = {

    val start = for {

      started <- Boards.filter(_.id === boardId)
        .filter(_.status === 0)
        .map(_.status).update(1).map(_ > 0)

      _ <- DBAction.touchBoard(boardId)

    } yield started

    db.run(start)
  }

  def getBoard(boardId: String): Future[Option[Board]] = {
    db.run(DBAction.getBoardById(boardId))
  }

  def boardExists(boardId: String): Future[Boolean] = {
    getBoard(boardId).map(_.isDefined)
  }

  def getPlayer(playerId: Int): Future[Option[Player]] = {
    db.run(DBAction.getPlayerById(playerId))
  }

  def getPlayers(boardId: String): Future[Seq[Player]] = {
    db.run(DBAction.getPlayersByBoard(boardId))
  }

  def getPlayerByUser(boardId: String, userId: Int): Future[Option[Player]] = {
    db.run(DBAction.getPlayerByUser(boardId, userId))
  }

  def getParticipants(boardId: String): Future[Seq[Participant]] = {

    val players = DBQuery.playersByBoard(boardId)
    val playersWithUsers = (players join Users on (_.userId === _.id)).result

    val participants = playersWithUsers.map(_.map {
      case (player, user) => Participant(player, user)
    })

    db.run(participants).map(_.sortBy(_.player.turnOrder))
  }

  def takeAction(boardId: String, actionId: Int, playerOrder: Int): Future[Unit] = {

    val action = for {
      actions <- DBAction.getActionsByBoard(boardId)
      _ <- Actions += Action(-1, boardId, actionId, actions.size, playerOrder)
      _ <- DBAction.touchBoard(boardId)
    } yield ()

    db.run(action)
  }

  def getActions(boardId: String): Future[Seq[Action]] = {
    db.run(DBAction.getActionsByBoard(boardId))
  }

  def deleteBoard(boardId: String): Future[Boolean] = {
    db.run(Boards.filter(_.id === boardId).delete).map(_ > 0)
  }

  def searchBoards(query: SearchQuery[BoardFilter]):
      Future[SearchResponse[Board]] = {
    
    val boards = query.filters.foldLeft[BoardsQuery] (Boards)
      { (q, f) => f match {

        case AllBoards => q

        case UserBoards(userId: Int) => q.filter { board =>
          Players
            .filter(_.userId === userId)
            .filter(_.boardId === board.id)
            .exists
        }

        case FriendsBoards(userId: Int) => q.filter { board =>

          val friends = users.DBQuery.friendsByUser(userId)

          friends.filter { friend => 
            Players
              .filter(_.userId === friend.id)
              .filter(_.boardId === board.id)
              .exists
          }.exists
        }

        case MostRecent => q.sortBy(_.modified.reverse)
      }}

    search.paginate(boards, query)
  }

  private def randomId() = {
    (random() * (1 << 20)).toInt.toHexString
      .toUpperCase.padTo(5, "0").toString
  }

  private def canJoin(board: Board, players: Seq[Player], userId: Int) = {
    players.size < Games(board.gameId).players.max &&
      !players.exists(_.userId == userId) &&
      board.status == 0
  }

  private[models] object DBQuery {

    def boardById(boardId: String) =
      Boards.filter(_.id === boardId)

    def playerById(playerId: Int) =
      Players.filter(_.id === playerId)

    def actionById(actionId: Int) =
      Actions.filter(_.id === actionId)

    def playersByBoard(boardId: String) =
      Players
        .filter(_.boardId === boardId)
        .sortBy(_.turnOrder)

    def playerByPosition(boardId: String, position: Int) =
      Players
        .filter(_.boardId === boardId)
        .filter(_.turnOrder === position)

    def playerByUser(boardId: String, userId: Int) =
      Players
        .filter(_.boardId === boardId)
        .filter(_.userId === userId)

    def actionsByBoard(boardId: String) =
      Actions
        .filter(_.boardId === boardId)
        .sortBy(_.actionOrder)
  }

  private[models] object DBAction {

    def getBoardById(boardId: String) =
      DBQuery.boardById(boardId).result.headOption

    def getPlayerById(playerId: Int) =
      DBQuery.playerById(playerId).result.headOption

    def getActionById(actionId: Int) =
      DBQuery.actionById(actionId).result.headOption

    def getPlayersByBoard(boardId: String) =
      DBQuery.playersByBoard(boardId).result

    def getPlayerByPosition(boardId: String, position: Int) =
      DBQuery.playerByPosition(boardId, position).result.headOption

    def getBoardWithPlayers(boardId: String) =
      for {
        board <- getBoardById(boardId)
        players <- getPlayersByBoard(boardId)
      } yield board.map((_, players))

    def getPlayersWithUsers(boardId: String) =
      (DBQuery.playersByBoard(boardId) join Users
        on (_.userId === _.id)).result

    def getPlayerWithUser(playerId: Int) =
      (DBQuery.playerById(playerId) join Users
        on (_.userId === _.id)).result.headOption

    def getPlayerByUser(boardId: String, userId: Int) =
      DBQuery.playerByUser(boardId, userId).result.headOption

    def getActionsByBoard(boardId: String) =
      DBQuery.actionsByBoard(boardId).result

    def swapPlayers(player1: Player, player2: Player) = {
      for {
        _ <- DBQuery.playerById(player1.id)
          .map(_.turnOrder).update(player2.turnOrder)
        _ <- DBQuery.playerById(player2.id)
          .map(_.turnOrder).update(player1.turnOrder)
      } yield ()
    }

    def addPlayer(boardId: String, userId: Int, turnOrder: Int) =
      for {
        _ <- Players += Player (
          userId=userId, boardId=boardId, turnOrder=turnOrder)
        player <- DBAction.getPlayerByUser(boardId, userId)
        _ <- DBAction.touchBoard(boardId)
      } yield player

    def touchBoard(boardId: String) =
      DBQuery.boardById(boardId)
        .map(_.modified)
        .update(LocalDateTime.now().toString)

    def transferBoard(boardId: String, playerId: Int) =
      DBQuery.playerById(playerId)
        .map(_.isOwner).update(true)

    def deleteBoard(boardId: String) =
      DBQuery.boardById(boardId).delete
  }
}