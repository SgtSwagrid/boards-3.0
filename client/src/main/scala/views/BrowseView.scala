package views

import scala.scalajs.js.annotation.JSExportTopLevel
import slinky.core.{Component, StatelessComponent}
import slinky.core.facade.ReactElement
import slinky.core.annotations.react
import org.scalajs.dom._
import slinky.web.html._
import slinky.web.ReactDOM
import io.circe.generic.auto._, io.circe.syntax._
import views.components.TabsComponent
import models.Board
import protocols.SearchProtocol._
import views.components.menu.PaginationComponent

object BrowseView {

  private val queryRoute = "/games/browse/query/"
  private val gameRoute  = "/games/board/"

  @JSExportTopLevel("browse")
  def browse() = {
    
    ReactDOM.render (
      BrowseComponent(),
      document.getElementById("root")
    )
  }

  @react class BrowseComponent extends Component {

    type Props = Unit
    case class State(tab: Int)
    val initialState = State(0)

    def render() = { println(state); div(className := "container") (
      TabsComponent(Seq (
        ("All Boards",      "/assets/img/earth-grid.svg"),
        ("Friend's Boards", "/assets/img/followers.svg"),
        ("My Boards",       "/assets/img/user.svg")
      )),
      BoardListComponent()
    )}
  }

  @react class BoardListComponent extends Component {

    type Props = Unit
    case class State(result: Int)
    val initialState = State(5)

    def render() = { println(state); div() }
      //state.result map { result => div (
        //result.items map { board =>
        //  GameComponent(board)
        //},
        //PaginationComponent(result.page, result.pages, query _)
      //)}

    //override def componentDidMount() = println(state)//query(0)

    /*private def query(page: Int) =

      FetchJson.postJson(queryRoute, SearchQuery(page)) {
        result: SearchResponse[Board] =>{println(result)
          setState(state.copy(result = Some(result)))
      }}*/
  }

  @react class GameComponent extends StatelessComponent {

    case class Props(board: Board)

    def render() = div (
      className := "menu-item block grey darken-3 z-depth-2 waves-effect hoverable",
      onClick := (_ => view(props.board))
    ) (
      div(className := "menu-item-body") (
        span(className := "white-text medium-text") (props.board.game.name),
        span(className := "grey-text small-text") (s"#${props.board.game.name}")
      )
    )

    private def view(board: Board) =
      window.location.href = gameRoute + board.id
  }
}