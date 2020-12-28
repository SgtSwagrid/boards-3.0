package views

import scala.concurrent.ExecutionContext.Implicits.global
import org.scalajs.dom.{document, html, window}
import org.scalajs.dom.ext.Ajax
import scala.scalajs.js.annotation.JSExportTopLevel
import slinky.core.{Component, StatelessComponent}
import slinky.core.facade.ReactElement
import slinky.core.annotations.react
import slinky.web.ReactDOM
import slinky.web.html._
import games.core._
import components.PaginationComponent

object CreateView {

  val createRoute = "/games/create/"
  val gameRoute   = "/games/board/"

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

    final val pageSize = 6

    def render() = div (
      div(className := "input-field") (
        input(className := "white-text", id := "search", `type` := "text", autoFocus,
          onChange := (e => search(e.target.value))
        ),
        label(className := "white-text", htmlFor := "search") ("Search games")
      ),
      state.games.drop(state.page * pageSize).take(pageSize).map { game =>
        div(key := game.name) (GameComponent(game))
      },
      PaginationComponent(state.page, pages, goto _)
    )
    
    private def search(string: String) = {

      val games = Manifest.Games.filter {
        _.name.toLowerCase.contains(string.toLowerCase)
      }
      val page = state.page min (games.size-1)
      setState(state.copy(games = games, page = 0))
    }

    private def goto(page: Int) =
      setState(state.copy(page = 0 max ((pages-1) min page)))
      
    private def pages =
      (((state.games.size - 1) / pageSize) + 1) max 1
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

    private def create(game: Game) = {

      FetchJson.post(createRoute + game.id) { boardId: String =>
        window.location.href = gameRoute + boardId
      }
    }
  }
}