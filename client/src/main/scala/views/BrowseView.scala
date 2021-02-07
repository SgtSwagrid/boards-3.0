package views

import org.scalajs.dom._, org.scalajs.dom.html
import scala.scalajs.js.annotation.JSExportTopLevel
import slinky.core.{Component, StatelessComponent}
import slinky.core.facade.ReactElement
import slinky.core.annotations.react
import slinky.web.html._
import slinky.web.ReactDOM
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import models.{Board, User}
import models.protocols.SearchProtocol._
import models.protocols.BoardProtocol._
import views.helpers.FetchJson
import views.menu.{PaginationComponent, ButtonComponent}
import views.menu.Tabs, views.menu.Tabs.Tab

object BrowseView {

  private val queryRoute = "/games/browse/query"
  private val gameRoute  = "/games/board/"

  @JSExportTopLevel("browse")
  def browse() = {
    
    val user = decode[User](document.getElementById("user")
      .asInstanceOf[html.Input].value).toOption.get

    ReactDOM.render (
      BrowseComponent(user),
      document.getElementById("root")
    )
  }

  @react class BrowseComponent extends Component {

    case class Props(user: User)
    case class State(tab: Int)
    def initialState = State(0)

    def render() = div(className := "container") (
      Tabs (
        Tab (
          "All Boards",
          "/assets/img/earth-grid.svg",
          BoardListComponent(AllBoards)
        ), Tab (
          "Friend's Boards",
          "/assets/img/followers.svg",
          BoardListComponent(FriendsBoards(props.user.id))
        ), Tab (
          "My Boards",
          "/assets/img/user.svg",
          BoardListComponent(MyBoards(props.user.id))
        )
      )
    )
  }

  @react class BoardListComponent extends Component {

    case class Props(filter: BoardFilter)
    case class State(result: Option[SearchResponse[Board]])
    def initialState = State(None)

    def render() =
      state.result map { result =>
        div(className := "row") (
          div (className := "col s12 m10 push-m1 l8 push-l2 xl6 push-xl3") (
            result.items map { board =>
              GameComponent(board)
            },
            PaginationComponent(result.page, result.pages, query _)
          )
        )
      }

    override def componentDidMount() = query(0)

    private def query(page: Int) = {

      val query = SearchQuery(Seq(props.filter, MostRecent), page)

      FetchJson.postJson(queryRoute, query) {
        result: SearchResponse[Board] =>
          setState(state.copy(result = Some(result)))
      }
    }
  }

  @react class GameComponent extends StatelessComponent {

    case class Props(board: Board)

    def render() = div (
      className := "menu-item block grey darken-3 z-depth-2 waves-effect hoverable",
      onClick := (_ => view(props.board))
    ) (
      div(className := "menu-item-body") (
        span(className := "white-text medium-text") (props.board.game.name),
        span(className := "grey-text small-text") (s" #${props.board.id}"),
        img(className := "btn-icon", src := "/assets/img/play.svg")
      )
    )

    private def view(board: Board) =
      window.location.href = gameRoute + board.id
  }
}