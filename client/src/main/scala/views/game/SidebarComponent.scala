package views.game

import org.scalajs.dom._

import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.web.html
import slinky.web.html._

import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

import models.{Board, Player, User}
import models.protocols.BoardProtocol._
import games.core.{History, State}
import games.core.History.AnyHistory
import games.core.State.AnyState
import views.menu.ButtonComponent

@react class SidebarComponent extends StatelessComponent {

    case class Props (
      board: Board,
      players: Seq[Player],
      users: Seq[User],
      currentHistory: AnyHistory,
      visibleHistory: AnyHistory,
      session: BoardSession,
      goto: AnyHistory => Unit
    )

    private lazy val game = props.board.game

    private type HistoryT = game.HistoryT

    private def currentHistory = props.currentHistory.asInstanceOf[HistoryT]
    private def visibleHistory = props.visibleHistory.asInstanceOf[HistoryT]

    def render() = div(className := "sidebar grey darken-2 z-depth-2") (
      div(className := "sidebar-header grey darken-3") (
        div(className := "medium-text white-text") (props.board.game.name),
        div(className := "small-text grey-text") ("#" + props.board.id)
      ),
      div(className := "sidebar-body") (
        (props.players zip props.users).map { case (player, user) =>
          PlayerComponent(props.board, player, user, visibleHistory, props.session)
        },
        br(),

        if (props.board.setup) div (

          Option.when(props.session.owner) { div (
            Option.when(props.board.game.players.contains(props.players.size)) {
              ButtonComponent("Start Game", "/assets/img/play.svg", true, start)
            },
            ButtonComponent("Cancel Game", "/assets/img/trash.svg", true, delete)
          )},

          props.session.player match {
            case Some(player) =>
              ButtonComponent("Leave Game", "/assets/img/cancel.svg", true, leave)
            case None =>
              ButtonComponent("Join Game", "/assets/img/join.svg", true, join)
          }

        ) else if (props.board.ongoing) div (

          

        ) else if (props.board.ended) div (

          Option.when(props.session.owner) {
            ButtonComponent("Delete Game", "/assets/img/remove.svg", true, delete)
          }

        ) else div(),
        
        br(),

        div(className := "center") (
          img(className := "medium-icon", src := "/assets/img/icon/first.svg",
            onClick := (_ => first())
          ),
          img(className := "medium-icon", src := "/assets/img/icon/previous.svg",
            onClick := (_ => previous())
          ),
          img(className := "medium-icon", src := "/assets/img/icon/next.svg",
            onClick := (_ => next())
          ),
          img(className := "medium-icon", src := "/assets/img/icon/last.svg",
            onClick := (_ => last())
          )
        )
      )
    )

    private def join() = {
      val action: BoardRequest = JoinGame(props.board.id, props.session.user.id)
      props.session.socket.send(action.asJson.toString)
    }

    private def leave() = {
      val action: BoardRequest = RemovePlayer(props.board.id, props.session.player.get.id)
      props.session.socket.send(action.asJson.toString)
    }

    private def delete() = {
      val action: BoardRequest = DeleteGame(props.board.id)
      props.session.socket.send(action.asJson.toString)
    }

    private def start() = {
      val action: BoardRequest = StartGame(props.board.id)
      props.session.socket.send(action.asJson.toString)
    }

    private def histories(history: HistoryT) =
      Iterator(history) ++
      Iterator.unfold(history)(h => h.previous zip h.previous)

    private def first() =
      props.goto(histories(visibleHistory).toSeq.last)

    private def previous() =
      visibleHistory.previous.foreach(props.goto)

    private def next() =
      histories(currentHistory)
        .find(_.previous.contains(props.visibleHistory))
        .foreach(props.goto)

    private def last() =
      props.goto(currentHistory)
  }

  @react class PlayerComponent extends StatelessComponent {

    case class Props (
      board: Board,
      player: Player,
      user: User,
      history: AnyHistory,
      session: BoardSession
    )

    private lazy val game = props.board.game

    private type StateT = game.StateT
    private type HistoryT = game.HistoryT

    private def history = props.history.asInstanceOf[HistoryT]
    private def gameState = history.state

    def render() = div(className := "sidebar-player") (
      span(className := "medium-text white-text") (
        if (props.player.isOwner) {
          img(className := "medium-text-icon", src := "/assets/img/owner.svg")
        } else {
          img(className := "medium-text-icon", src := "/assets/img/user.svg")
        },
        props.user.username
      ),
      Option.when(props.session.owner && props.board.setup) {
        span(className := "player-options") (
          a(className := "yellow darken-2 waves-effect player-promote",
              onClick := (_ => promote())) (
            img(className := "order-icon", src := "/assets/img/up-arrow.svg")
          ),
          a(className := "yellow darken-2  waves-effect player-demote",
              onClick := (_ => demote())) (
            img(className := "order-icon", src := "/assets/img/down-arrow.svg")
          ),
          a(className := "red darken-4 waves-effect player-remove",
              onClick := (_ => remove())) (
            img(className := "remove-icon", src := "/assets/img/remove.svg")
          )
        )
      },
      div(className := "small-text white-text") (
        (Option.when(props.player.turnOrder == gameState.turn) {
          "Playing"
        } ++ Option.when(gameState.players.exists(_.score != 0)) {
          s"${gameState.players(props.player.turnOrder)} pts"
        }).mkString(" &bull; ")
      ),
      hr()
    )

    private def remove() = {
      val action: BoardRequest = RemovePlayer(props.board.id, props.player.id)
      props.session.socket.send(action.asJson.toString)
    }

    private def promote() = {
      val action: BoardRequest = PromotePlayer(props.board.id, props.player.id)
      props.session.socket.send(action.asJson.toString)
    }

    private def demote() = {
      val action: BoardRequest = DemotePlayer(props.board.id, props.player.id)
      props.session.socket.send(action.asJson.toString)
    }
  }