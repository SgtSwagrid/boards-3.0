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

    for {
      _ <- db.run(Boards += board) 
      _ <- db.run(Players += player)
      Some(board) <- db.run(boardById(boardId))
    } yield board
  }

  def joinBoard(boardId: String, userId: Int): Future[Option[Player]] = {

    db.run(boardWithPlayers(boardId)) flatMap {

      case Some((board, players))
        if canJoin(board, players, userId) => {

        val player = Player(userId=userId,
          boardId=boardId, turnOrder=players.size)

        for {
          _ <- db.run(Players += player)
          player <- db.run(playerByUser(boardId, userId))
          _ <- db.run(touchBoard(boardId))
        } yield player
      }
      case _ => Future.successful(None)
    }
  }

  def removePlayer(boardId: String, playerId: Int): Future[Boolean] = {

    for {
      // Find the player associated with this user.
      player <- db.run(playerById(playerId) map(_.get))

      // Leave the game.
      result <- db.run(Players.filter(_.id === playerId).delete).map(_ > 0)

      // Get the remaining players.
      players <- db.run(playersByBoard(boardId))

      // Move each subsequent player up by one position.
      _ <- db.run(DBIOAction.sequence(players
        .filter(_.turnOrder > player.turnOrder)
        .map(player => Players.filter(_.id === player.id)
          .map(_.turnOrder).update(player.turnOrder-1))))

      // Delete the board if all players leave.
      _ <- if (players.size == 0)
          db.run(Boards.filter(_.id === boardId).delete)

      // Promote another player if the owner leaves.
        else if (player.isOwner)
          db.run(Players.filter(_.id === players.head.id)
            .map(_.isOwner).update(true))

        else Future.unit

      _ <- db.run(touchBoard(boardId))

    } yield result
  }

  def promotePlayer(boardId: String, playerId: Int): Future[Boolean] = {

    val promote = for {

      player <- playerById(playerId)

      previous <- playerByPosition(boardId,
        player.map(_.turnOrder-1).getOrElse(-1))

      swapped <- (player zip previous) match {
        case Some((player, previous)) =>
          swap(player, previous).map(_ => true)
        case None => DBIO.successful(false)
      }

      _ <- touchBoard(boardId)

    } yield swapped

    db.run(promote)
  }

  def demotePlayer(boardId: String, playerId: Int): Future[Boolean] = {

    val demote = for {

      player <- playerById(playerId)

      next <- playerByPosition(boardId,
        player.map(_.turnOrder+1).getOrElse(-1))

      swapped <- (player zip next) match {
        case Some((player, next)) => swap(player, next).map(_ => true)
        case None => DBIO.successful(false)
      }

      _ <- touchBoard(boardId)
      
    } yield swapped

    db.run(demote)
  }

  def getBoard(boardId: String): Future[Option[Board]] = {
    db.run(boardById(boardId))
  }

  def boardExists(boardId: String): Future[Boolean] = {
    getBoard(boardId).map(_.isDefined)
  }

  def takeAction(boardId: String, action: Int): Future[Unit] = {

    for {
      actions <- db.run(actionsByBoard(boardId))
      _ <- db.run(Actions += ActionRow(-1, boardId, action, actions.size))
      _ <- db.run(touchBoard(boardId))
    } yield ()
  }

  def getPlayer(playerId: Int): Future[Option[Player]] = {
    db.run(playerById(playerId))
  }

  def getPlayers(boardId: String): Future[Seq[Player]] = {
    db.run(playersByBoard(boardId))
  }

  def getParticipants(boardId: String): Future[Seq[Participant]] = {

    val players = Players.filter(_.boardId === boardId).sortBy(_.turnOrder)
    val playersWithUsers = (players join Users on (_.userId === _.id)).result

    val participants = playersWithUsers.map(_.map {
      case (player, user) => Participant(player, user)
    })

    db.run(participants).map(_.sortBy(_.player.turnOrder))
  }

  def startGame(boardId: String): Future[Boolean] = {

    val start = for {

      result <- Boards.filter(_.id === boardId)
        .filter(_.status === 0)
        .map(_.status).update(1).map(_ > 0)

      _ <- touchBoard(boardId)

    } yield result

    db.run(start)
  }

  def deleteBoard(boardId: String): Future[Boolean] = {
    db.run(Boards.filter(_.id === boardId).delete).map(_ > 0)
  }

  def searchBoards(query: SearchQuery[BoardFilter], userId: Int):
      Future[SearchResponse[Board]] = {
    
    val boards = query.filters.foldLeft[BoardsQuery] (Boards)
      { (q, f) => f match {

        case AllBoards => q

        case FriendsBoards => q

        case MyBoards => q.filter { b =>
          Players
            .filter(_.userId === userId)
            .filter(_.boardId === b.id)
            .exists
        }

        case MostRecent => q.sortBy(_.modified.reverse)
      }}

    search.paginate(boards, query)
  }

  private def touchBoard(boardId: String) = {
    Boards.filter(_.id === boardId).map(_.modified)
      .update(LocalDateTime.now().toString)
  }

  private def boardById(boardId: String) = {
    Boards.filter(_.id === boardId).result.headOption
  }

  private def playersByBoard(boardId: String) = {
    Players.filter(_.boardId === boardId).sortBy(_.turnOrder).result
  }

  private def playerByPosition(boardId: String, position: Int) = {
    Players.filter(_.boardId === boardId)
      .filter(_.turnOrder === position).result.headOption
  }

  private def boardWithPlayers(boardId: String): DBIO[Option[(Board, Seq[Player])]] = {
    for {
      board <- boardById(boardId)
      players <- playersByBoard(boardId)
    } yield board.map((_, players))
  }

  private def playersWithUsers(boardId: String) = {
    val players = Players.filter(_.boardId === boardId)
    (players join Users on (_.userId === _.id)).result
  }

  private def playerById(playerId: Int) = {
    Players.filter(_.id === playerId).result.headOption
  }

  private def playerWithUser(playerId: Int) = {
    playerById(playerId) flatMap {
      case Some(player) =>
        users.userById(player.userId)
          .map { _ map { user => Some((player, user)) }}
      case None => DBIOAction.successful(None)
    }
  }

  private def swap(player1: Player, player2: Player) = {
    for {
      _ <- Players.filter(_.id === player1.id)
        .map(_.turnOrder).update(player2.turnOrder)
      _ <- Players.filter(_.id === player2.id)
        .map(_.turnOrder).update(player1.turnOrder)
    } yield ()
  }

  private def actionsByBoard(boardId: String) = {
    Actions.filter(_.boardId === boardId).sortBy(_.actionOrder).result
  }

  private def playerByUser(boardId: String, userId: Int) = {
    Players
      .filter(_.boardId === boardId)
      .filter(_.userId === userId)
      .result.headOption
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
}