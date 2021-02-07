package views.components

import scala.concurrent.ExecutionContext.Implicits.global
import org.scalajs.dom.{document, html, window}
import org.scalajs.dom.ext.Ajax
import scala.scalajs.js.annotation.JSExportTopLevel
import slinky.core.{Component, StatelessComponent}
import slinky.core.facade.ReactElement
import slinky.core.annotations.react
import slinky.web.ReactDOM
import slinky.web.html._
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import models.Board, games.core._
import views.menu.{LocalMenuComponent, MenuItem}
import views.helpers.FetchJson
import games.core.Game

object CreateView {

  private val createRoute = "/games/create/"
  private val gameRoute   = "/games/board/"

  @JSExportTopLevel("create")
  def create() = {

    ReactDOM.render (
      CreateComponent(),
      document.getElementById("root")
    )
  }

  @react class CreateComponent extends StatelessComponent {

    type Props = Unit

    def render() = div(className := "container") (
      br(), h4(className := "white-text center")("Start a new game."),
      div(className := "row") (
        div(className := "col s12 m10 push-m1 l8 push-l2 xl6 push-xl3") (
          LocalMenuComponent (
            Manifest.Games map { game => MenuItem (
              GameComponent(game), List(game.name)
            )}
          )
        )
      )
    )
  }

  @react class GameComponent extends StatelessComponent {

    case class Props(game: Game)

    def render() = div (
      className := "menu-item block grey darken-3 z-depth-2 waves-effect hoverable",
      onClick := (_ => create(props.game))
    ) (
      div(className := "menu-item-body") (
        span(className := "white-text medium-text") (props.game.name),
        img(className := "btn-icon", src := "/assets/img/play.svg")
      )
    )

    private def create(game: Game) =
      FetchJson.post(createRoute + game.id) { board: Board =>
        window.location.href = gameRoute + board.id
      }
  }
}