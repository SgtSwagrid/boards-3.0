package views

import org.scalajs.dom._
import scala.scalajs.js.annotation.JSExportTopLevel
import slinky.core.{Component, StatelessComponent}
import slinky.core.facade.ReactElement
import slinky.core.annotations.react
import slinky.web.ReactDOM
import slinky.web.html._
import games.core._
import components.PaginationComponent

object CreateView {

  @JSExportTopLevel("create")
  def create() = {

    ReactDOM.render (
      MenuComponent(),
      document.getElementById("root")
    )
  }

  @react class MenuComponent extends Component {

    type Props = Unit
    case class State(games: List[Game], page: Int)
    def initialState = State(Manifest.Games, 0)

    final val gamesPerPage = 6

    def render() = div (
      div(className := "input-field") (
        input(className := "white-text", id := "search", `type` := "text",
          onChange := (e => search(e.target.value))
        ),
        label(className := "white-text", htmlFor := "search") ("Search games")
      ),
      state.games.drop(state.page * gamesPerPage).take(gamesPerPage).map { game =>
        div(key := game.name) (GameComponent(game))
      },
      PaginationComponent(state.page, pages, goto _)
    )
    
    private def search(string: String) = {

      val games = Manifest.Games.filter {
        _.name.toLowerCase.contains(string.toLowerCase)
      }
      val page = Math.min(state.page, games.size-1)
      setState(state.copy(games = games, page = 0))
    }

    private def goto(page: Int) =
      setState(state.copy(page = Math.max(0, Math.min(pages-1, page))))
      
    private def pages =
      Math.max(((state.games.size - 1) / gamesPerPage) + 1, 1)
  }

  @react class GameComponent extends StatelessComponent {

    case class Props(game: Game)

    def render() = div(
        className := "menu-item block grey darken-3 z-depth-2 waves-effect") (
      div(className := "menu-item-body") (
        span(className := "white-text medium-text") (props.game.name)
      )
    )
  }
}