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
import views.components.menu.{LocalMenuComponent, MenuItem}
import views.helpers.FetchJson

object CreateView {

  private val createRoute = "/games/create/"
  private val gameRoute   = "/games/board/"

  @JSExportTopLevel("create")
  def create() = {

    ReactDOM.render (

      LocalMenuComponent (
        Manifest.Games map { game => MenuItem (
          GameComponent(game), List(game.name)
        )}
      ),
      
      document.getElementById("root")
    )
  }

  @react class GameComponent extends StatelessComponent {

    case class Props(game: Game)

    def render() = div (
      className := "menu-item block grey darken-3 z-depth-2 waves-effect hoverable",
      onClick := (_ => create(props.game))
    ) (
      div(className := "menu-item-body") (
        span(className := "white-text medium-text") (props.game.name)
      )
    )

    private def create(game: Game) =
      FetchJson.post(createRoute + game.id) { board: Board =>
        window.location.href = gameRoute + board.id
      }
  }
}