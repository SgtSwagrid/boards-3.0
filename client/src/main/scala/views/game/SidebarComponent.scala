package views.game

import org.scalajs.dom._

import slinky.core.{Component, StatelessComponent}
import slinky.core.annotations.react
import slinky.web.html
import slinky.web.html._

import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

import models.{Board, Participant}
import models.protocols.BoardProtocol._
import views.menu.ButtonComponent

@react class SidebarComponent extends Component {

    case class Props(board: Board, players: Seq[Participant], session: BoardSession)
    type State = Unit
    def initialState = ()

    def render() = div(className := "sidebar grey darken-2 z-depth-2") (
      div(className := "sidebar-header grey darken-3") (
        div(className := "medium-text white-text") (props.board.game.name),
        div(className := "small-text grey-text") ("#" + props.board.id)
      ),
      div(className := "sidebar-body") (
        props.players.map(player => PlayerComponent(props.board, player, props.session)),
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

        ) else div()
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
  }

  @react class PlayerComponent extends StatelessComponent {

    case class Props(board: Board, player: Participant, session: BoardSession)

    def render() = div(className := "sidebar-player") (
      span(className := "medium-text white-text") (
        if (props.player.player.isOwner) {
          img(className := "medium-icon", src := "/assets/img/owner.svg")
        } else {
          img(className := "medium-icon", src := "/assets/img/user.svg")
        },
        props.player.user.username
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
      }
    )

    private def remove() = {
      val action: BoardRequest = RemovePlayer(props.board.id, props.player.player.id)
      props.session.socket.send(action.asJson.toString)
    }

    private def promote() = {
      val action: BoardRequest = PromotePlayer(props.board.id, props.player.player.id)
      props.session.socket.send(action.asJson.toString)
    }

    private def demote() = {
      val action: BoardRequest = DemotePlayer(props.board.id, props.player.player.id)
      props.session.socket.send(action.asJson.toString)
    }
  }