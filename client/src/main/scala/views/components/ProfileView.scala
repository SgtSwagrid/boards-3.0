package views.components

import org.scalajs.dom._, org.scalajs.dom.html
import scala.scalajs.js.annotation.JSExportTopLevel
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import slinky.core.{Component, StatelessComponent}
import slinky.core.facade.ReactElement
import slinky.core.annotations.react
import slinky.web.ReactDOM, slinky.web.html._
import models.User, games.core._
import views.components.menu.PaginationComponent
import views.components.ButtonComponent
import views.components.Tabs.Tab
import java.awt.MenuComponent
import models.protocols.BoardProtocol.FriendsBoards
import models.protocols.UserProtocol._
import slinky.web.svg.result
import slinky.web.svg.requiredExtensions
import models.protocols.SearchProtocol.SearchResponse
import models.protocols.SearchProtocol.SearchQuery
import views.helpers.FetchJson
import views.components.menu.SearchComponent
import slinky.core.facade.ErrorBoundaryInfo


object ProfileView {

  private val userQueryRoute = "/users/query"
  private val userRoute = "/users/profile/"


  @JSExportTopLevel("profileView")
  def profile() = {

    val allUsers = Manifest.Users
    val user = decode[User](document.getElementById("user").asInstanceOf[html.Input].value).toOption.get
    val profileUser = decode[User](document.getElementById("profileUser").asInstanceOf[html.Input].value).toOption.get

    ReactDOM.render (
      //MenuComponent(allUsers, user),
      BrowseComponent(user, profileUser),
      document.getElementById("root")
    )
  }

  @react class BrowseComponent extends Component {

    case class Props(user: User, profileUser: User)
    case class State(tab: Int)
    def initialState = State(0)

    val detailsHref: String = "tabDetails"
    val friendsHref: String = "tabFriends"

    def render() = div(className := "container") (
      Tabs(
        Tab (
          "Details",
          "/assets/img/user.svg",
          DetailsComponent(props.user, props.profileUser)
        ),
        Tab (
          "Friends",
          "/assets/img/followers.svg",
          if (props.user.id == props.profileUser.id) {
            FriendsComponent(props.user, props.profileUser)
          } else {
            div()  // todo view their friends
          }
        )
      )
    )
  }

@react class DetailsComponent extends StatelessComponent {

  case class Props(user: User, profileUser: User)

  def render() = {
    div(className:="col s12")(
    
      br(),
      div(className:="container center-align") (
        h3(className:="white-text") (props.user.username),
        p(className:="white-text") (
          span(className:="green-text") ("0 Wins \u2022 "), 
          span(className:="yellow-text") ("0 Draws \u2022 "), 
          span(className:="red-text") ("0 Losses")
        )
      )
    )
  }
}

@react class FriendsSearchComponent extends Component {

  case class Props(user: User, profileUser: User)
  case class State(users: List[User], page: Int)
  def initialState = State(List(), 0)

  def render() = {
    div()
  }


}

@react class FriendsComponent extends Component {

  case class Props(user: User, profileUser: User)
  case class State(searchUsername: String)
  def initialState = State("")

  def render() = {
    div(
      SearchComponent("Search Users", search _),
      UserListComponent(state.searchUsername, props.user)
    )
  }

  private def search(username: String) = {
      setState(state.copy(searchUsername = username))
  }

}

@react class UserListComponent extends Component {

  case class Props(filter: String, requester: User)
  case class State(result: Option[SearchResponse[User]])
  def initialState: State = State(None)

  def render() = {
    state.result map { result => div(
      result.items map { user =>
        UserComponent(user, props.requester)
      },
      PaginationComponent(result.page, result.pages, query _)
    )}
  }

  override def componentDidMount(): Unit = query(0)

  override def componentDidUpdate(prevProps: Props, prevState: State): Unit = {
    if (props.filter != prevProps.filter) {
      query(0)
    }
  }

  private def query(page: Int) = {

    val filter: UserFilter = NameContainsSubstring(props.filter)
    val query = SearchQuery(Seq(filter, NameAlphabetical), page)

    FetchJson.postJson(userQueryRoute, query) {
      result: SearchResponse[User] => setState(state.copy(result = Some(result)))
    }

  }
}

@react class MenuComponent extends Component {

  case class Props(allUsers: List[User], requester: User)
  case class State(users: List[User], page: Int)
  def initialState = State(currentFriends, 0)

  final val pageSize = 10

  def render() = div (
    div(className := "input-field") (
      input(className := "white-text", id := "search", `type` := "text", autoFocus, onChange := (e => searchUsers(e.target.value))),
      label(className := "white-text", htmlFor := "search") ("Search users")
    ),
    state.users.drop(state.page * pageSize).take(pageSize).map { user =>
      div(key := user.username) (UserComponent(user, props.requester))
    },
    PaginationComponent(state.page, pages, goto _ )  // goto _ == page => goto page
  )

  def searchUsers(string: String) = {
    if (string == "") {
      val users = currentFriends()
      setState(state.copy(users = users, page = 0))
    } else {
      val users = props.allUsers.filter {
        _.username.toLowerCase.contains(string.toLowerCase)
      }
      setState(state.copy(users = users, page = 0))
    }
  }

  def currentFriends() = {
    props.allUsers.filter(_.username == "Alice")
  }

  private def goto(page: Int) =
    setState(state.copy(page = 0 max ((pages-1) min page)))

  private def pages =
    (((state.users.size - 1) / pageSize) + 1) max 1

}

@react class UserComponent extends StatelessComponent {

  // Display the user a row in the list with clickable buttons 

  case class Props(user: User, requester: User)

  def render() = span(
      className := "menu-item block grey darken-3 z-depth-2 waves-effect hoverable",
      onClick := (_ => view(props.user))
    ) (
      div(className := "col s6 left-align white-text medium-text") (props.user.username)
      ,
      // button if the user is not friends yet
      ButtonComponent("Add", "/assets/img/add-user.svg", false, addFriend)
    )

  private def view(user: User) =
    window.location.href = userRoute + user.username

  private def addFriend() = {
    // todo
  }

}

}

