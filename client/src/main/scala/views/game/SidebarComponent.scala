package views.game

import scala.collection.decorators._

import scala.scalajs.js
import org.scalajs.dom._

import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.web.html
import slinky.web.html._

import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

import models.{Board, Player, User}
import models.protocols.BoardProtocol._
import games.core.{Action, History, State}
import games.core.History.AnyHistory
import games.core.State.AnyState
import views.menu.ButtonComponent
import slinky.core.facade.ReactElement

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
        
        Option.when (
          visibleHistory.state.outcome == State.Ongoing &&
          props.users.isDefinedAt(visibleHistory.state.turn)
        ) {

          val user = props.users(visibleHistory.state.turn)
          
          span(className := "medium-text yellow-text text-darken-2") (
            if (user == props.session.user) "Your Turn"
            else s"${user.username}'s Turn"
          )
        },

        br(), br(),

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

        ) else div()
        
      ),
      
      div(className := "sidebar-nav blue-grey darken-4") (

        div(className := "center") (
          div (
            className := "nav-icon",
            onClick := (_ => first())
          ) (
            img (
              className := "medium-icon",
              src := "/assets/img/icon/first.svg"
            )
          ),
          div (
            className := "nav-icon",
            onClick := (_ => previous())
          ) (
            img (
              className := "medium-icon",
              src := "/assets/img/icon/previous.svg"
            )
          ),
          div (
            className := "nav-icon",
            onClick := (_ => next())
          ) (
            img (
              className := "medium-icon",
              src := "/assets/img/icon/next.svg"
            )
          ),
          div (
            className := "nav-icon",
            onClick := (_ => last())
          ) (
            img (
              className := "medium-icon",
              src := "/assets/img/icon/last.svg"
            )
          )
        )
      ),
      
      div(className := "sidebar-footer grey darken-3") (
        
        currentHistory.histories.reverse.flatMap { history =>
          history.action map { action =>

            val textColour = s"${if (history == visibleHistory) "green" else "white"}-text"

            span (
              className := s"small-text $textColour",
              onClick := (_ => props.goto(history)),
              style := js.Dynamic.literal(display = "inline-block")
            ) (

              action match {

                case Action.Place(pos, piece) => {

                  span (
                    img (
                      className := "small-text-icon",
                      src := s"/assets/img/${piece.texture}"
                    ),
                    s"$pos",
                  )
                }

                case Action.Move(from, to) => {

                  val piece = history.state.pieces(to.asInstanceOf[game.VecT])

                  span (
                    img (
                      className := "small-text-icon",
                      src := s"/assets/img/${piece.texture}"
                    ),
                    s"$from → $to",
                  )
                }

                case _ => div()
              }
            )
          }
        }.intersperse(span(className := "small-text white-text")(" •"))
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

    private def first() =
      props.goto(visibleHistory.histories.last)

    private def previous() =
      visibleHistory.previous.foreach(props.goto)

    private def next() =
      currentHistory.histories
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