package views.game

import org.scalajs.dom._
import org.scalajs.dom.html
import scala.scalajs.js.annotation.JSExportTopLevel

import slinky.web.ReactDOM

import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

import models.User

object BoardView {

  lazy val socketRoute = document.getElementById("socketRoute")
    .asInstanceOf[html.Input].value.replace("http", "ws")

  @JSExportTopLevel("board")
  def board() = {

    val boardId = document.getElementById("boardId")
      .asInstanceOf[html.Input].value

    val user = decode[User](document.getElementById("user")
      .asInstanceOf[html.Input].value).toOption.get

    val socket = new WebSocket(socketRoute)
    socket.onclose = _ => window.location.reload()

    ReactDOM.render (
      GameComponent(boardId, user, socket),
      document.getElementById("root")
    )
  }
}