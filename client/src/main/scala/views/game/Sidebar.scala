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
import games.core.{Action, State}
import games.core.State.AnyState
import views.menu.ButtonComponent
import slinky.core.facade.ReactElement

@react class SidebarComponent extends StatelessComponent {

  private def gameRoute(boardId: String) = s"/games/board/$boardId"

  case class Props (
    session: Session.AnySession,
    update: Session.AnySession => Unit
  )

  private def session = props.session
  private lazy val game = session.game

  def render() = div(className := "sidebar grey darken-2 z-depth-2") (

    div(className := "sidebar-header grey darken-3") (

      div(className := "medium-text white-text") (session.game.name),
      span(className := "small-text grey-text text-lighten-1") ("#" + session.board.id),
      
      session.board.rematchBaseId.map { rematch =>
        span(className := "small-text blue-grey-text text-lighten-1") (
          " [Rematch of ", a(href := gameRoute(rematch))("#" + rematch), "]"
        )

      } orElse session.board.forkBaseId.map { fork =>
        span(className := "small-text blue-grey-text text-lighten-1") (
          " [Forked from ", a(href := gameRoute(fork))("#" + fork), "]"
        )
      }
    ),
    
    div(className := "sidebar-body") (

      PlayersComponent(session), br(),
      StatusComponent(session), br(), br(),
      MenuComponent(session), br(),
    ),

    BrowserComponent(session, props.update)
  )
}

@react class PlayersComponent extends StatelessComponent {

  case class Props(session: Session.AnySession)
  private def session = props.session

  def render() = (session.players zip session.users).map { case (player, user) =>

    div(className := "sidebar-player") (

      span(className := "medium-text white-text") (
        if (player.isOwner) {
          img(className := "medium-text-icon", src := "/assets/img/owner.svg")
        } else {
          img(className := "medium-text-icon", src := "/assets/img/user.svg")
        },
        user.username
      ),

      Option.when(props.session.isOwner && session.status == Session.Setup) {
        span(className := "player-options") (
          a(className := "yellow darken-2 waves-effect player-promote",
              onClick := (_ => session.promotePlayer(player))) (
            img(className := "order-icon", src := "/assets/img/up-arrow.svg")
          ),
          a(className := "yellow darken-2  waves-effect player-demote",
              onClick := (_ => session.demotePlayer(player))) (
            img(className := "order-icon", src := "/assets/img/down-arrow.svg")
          ),
          a(className := "red darken-4 waves-effect player-remove",
              onClick := (_ => session.removePlayer(player))) (
            img(className := "remove-icon", src := "/assets/img/remove.svg")
          )
        )
      },

      div(className := "small-text white-text") (
        (Option.when(player.turnOrder == session.visibleState.turn) {
          "Playing"
        } ++ Option.when(session.visibleState.players.exists(_.score != 0)) {
          s"${session.visibleState.players(player.turnOrder)} pts"
        }).mkString(" &bull; ")
      ),

      hr()
    )
  }
}

@react class StatusComponent extends StatelessComponent {

  case class Props(session: Session.AnySession)
  private def session = props.session

  def render() = session.status match {

    case Session.Setup => {
      span(className := "medium-text yellow-text text-darken-2") ("Pending")
    }

    case Session.Playing | Session.Ended => session.visibleOutcome match {

      case State.Ongoing => {

        val turn = session.visibleState.turn

        Option.when(session.users.isDefinedAt(turn)) {

          val user = session.users(turn)
          
          span(className := "medium-text yellow-text text-darken-2") (
            if (user == session.user) "Your Turn"
            else s"${user.username}'s Turn"
          )
        }
      }

      case State.Winner(winnerId) => {

        Option.when(session.users.isDefinedAt(winnerId)) {

          val user = session.users(winnerId)

          span(className := "medium-text yellow-text text-darken-2") (
            if (user == session.user) "You Won"
            else s"${user.username} Won"
          )
        }
      }

      case State.Draw => {
        span(className := "medium-text yellow-text text-darken-2") ("Draw")
      }
    }
  }
}

@react class MenuComponent extends StatelessComponent {

  case class Props(session: Session.AnySession)
  private def session = props.session

  def render() = session.status match {

    case Session.Setup => div (

      Option.when(session.isOwner) { div (
        Option.when(session.board.game.players.contains(session.players.size)) {
          ButtonComponent("Start Game", "/assets/img/play.svg", true, session.startGame)
        },
        ButtonComponent("Cancel Game", "/assets/img/trash.svg", true, session.deleteGame)
      )},

      props.session.player match {
        case Some(player) =>
          ButtonComponent("Leave Game", "/assets/img/cancel.svg", true, session.leaveGame)
        case None =>
          ButtonComponent("Join Game", "/assets/img/join.svg", true, session.joinGame)
      }
    )

    case Session.Playing =>

      Option.when(props.session.player.isDefined) { div (

        ButtonComponent (
          "Resign Game", "/assets/img/icon/resign.svg",
          true, session.resignGame
        ),

        span(className := "small-text white-text") (
          (session.players zip session.users)
            .filter { case (p, _) => p.resign }
            .map { case (_, u) => u.username }
            .intersperse(" • ")
        ),

        ButtonComponent (
          if (session.drawnUsers.contains(session.user)) "Draw Offered"
          else if (session.drawnPlayers.size > 0) "Accept Draw"
          else "Offer Draw",
          "/assets/img/icon/draw.svg", true, session.drawGame
        ),

        span(className := "small-text white-text") (
          session.drawnUsers.map(_.username).intersperse(" • ")
        ),
      )}

    case Session.Ended =>
    
      Option.when(session.player.isDefined) { div (

        Option.when(session.isOwner) {
          ButtonComponent("Delete Game", "/assets/img/trash.svg", true, session.deleteGame)
        },

        ButtonComponent (
          if (session.rematch.isDefined) "Accept Rematch" else "Offer Rematch",
          "/assets/img/icon/rematch.svg", true, session.rematchGame
        ),

        ButtonComponent (
          "Fork Game",
          "/assets/img/icon/fork.svg", true, session.forkGame
        )
      )}
  }
}

@react class BrowserComponent extends StatelessComponent {

  case class Props(session: Session.AnySession, update: Session.AnySession => Unit)
  private def session = props.session
  private lazy val game = session.game

  def render() = div (
  
    div(className := "sidebar-nav blue-grey darken-4") (

      div(className := "center") (
        div (
          className := "nav-icon",
          onClick := (_ => props.update(session.gotoFirst))
        ) (
          img (
            className := "medium-icon",
            src := "/assets/img/icon/first.svg"
          )
        ),
        div (
          className := "nav-icon",
          onClick := (_ => props.update(session.gotoPrevious))
        ) (
          img (
            className := "medium-icon",
            src := "/assets/img/icon/previous.svg"
          )
        ),
        div (
          className := "nav-icon",
          onClick := (_ => props.update(session.gotoNext))
        ) (
          img (
            className := "medium-icon",
            src := "/assets/img/icon/next.svg"
          )
        ),
        div (
          className := "nav-icon",
          onClick := (_ => props.update(session.gotoLast))
        ) (
          img (
            className := "medium-icon",
            src := "/assets/img/icon/last.svg"
          )
        )
      )
    ),
    
    div(className := "sidebar-footer grey darken-3") {

      val actions = for {
        state <- session.currentState.history.reverse
        action <- state.action
      } yield {

        val textColour = if (state == session.visibleState)
          "green-text" else "white-text"

        span (
          className := s"small-text $textColour",
          onClick := (_ => props.update(session.goto(state))),
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

              val piece = state.asInstanceOf[game.StateT]
                .pieces(to.asInstanceOf[game.VecT])

              span (
                img (
                  className := "small-text-icon",
                  src := s"/assets/img/${piece.texture}"
                ),
                s"$from → $to",
              )
            }

            case _ => span()
          }
        )
      }

      actions.intersperse(span(className := "small-text white-text")(" •"))
    }
  )
}