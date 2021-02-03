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
  private def friendRequestRoute(userId: Int) = s"/users/friends/create/$userId"

  @JSExportTopLevel("profileView")
  def profile() = {

    val allUsers = Manifest.Users
    val user = decode[User](document.getElementById("user")
      .asInstanceOf[html.Input].value).toOption.get
    val profileUser = decode[User](document.getElementById("profileUser")
      .asInstanceOf[html.Input].value).toOption.get

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

    def render() = {

      val userOwnsProfile: Boolean = props.user.id == props.profileUser.id

      val profileTab: Tab = Tab (
          "Details",
          "/assets/img/user.svg",
          ProfileDetailsComponent(props.user, props.profileUser)
        )

      val friendsTab: Tab = Tab (
          if (userOwnsProfile) { "My Friends" } else { "Friends"},
          "/assets/img/followers.svg",
          FriendsComponent(props.user, props.profileUser)
        )

      val addFriendsTab: Tab = Tab (
          "Add Friends",
          "/assets/img/add-user.svg",
          SearchFriendsComponent(props.user, props.profileUser)
        )

      div(className := "container") (
        Tabs (
          Seq(profileTab, friendsTab) ++
          Option.when(userOwnsProfile)(addFriendsTab) :_*
        )
      )
    }
      
  }

  @react class ProfileDetailsComponent extends StatelessComponent {

    /* Display a users profile. Public stats such as wins losses and draws are shown here.*/

    case class Props(user: User, profileUser: User)

    def render() = {
      div(className:="col s12")(
      
        br(),
        div(className:="container center-align") (
          h3(className:="white-text") (props.profileUser.username),
          p(className:="white-text") (
            span(className:="green-text") ("0 Wins \u2022 "), 
            span(className:="yellow-text") ("0 Draws \u2022 "), 
            span(className:="red-text") ("0 Losses")
          )
        )
      )
    }
  }

  @react class FriendsComponent extends StatelessComponent {

    /* Component to list the friends a user currently has. WIP. */ 

    case class Props(user: User, profileUser: User)

    def render() = {
      div(
        // Is friends filter todo
        UserListComponent(NameContainsSubstring(""), props.user)
      )
    }
  }

  @react class SearchFriendsComponent extends Component {

    /* Component to search and add new friends from. */ 

    case class Props(user: User, profileUser: User)
    case class State(searchUsername: String)
    def initialState = State("")

    def render() = {
      div(
        SearchComponent("Search Users", search _),
        UserListComponent(NameContainsSubstring(state.searchUsername), props.user)
      )
    }

    private def search(username: String) = {
        setState(state.copy(searchUsername = username))
    }

  }

  @react class UserListComponent extends Component {

    case class Props(filter: UserFilter, requester: User)
    case class State(result: Option[SearchResponse[User]])
    def initialState: State = State(None)

    def render() = {
      state.result map { result => div(
        result.items map { user =>
          UserComponent(props.requester, user)
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

      val query = SearchQuery(Seq(props.filter, NameAlphabetical), page)

      FetchJson.postJson(userQueryRoute, query) {
        result: SearchResponse[User] => setState(state.copy(result = Some(result)))
      }

    }
  }

  @react class UserComponent extends StatelessComponent {

    /* Display the user a row in the list with clickable buttons. If the user is already friends
      there is no button to add them as a friend, Instead a check mark is shown.*/

    case class Props(requester: User, target: User)

    def render() = {
      div (
        className := "menu-item block grey darken-3 z-depth-2 waves-effect hoverable",
        onClick := (_ => view(props.target))
      ) (
        div(className:= "row z-depth-5") (
          div(className:= "col s6") (
            div(className := "white-text medium-text") (props.target.username),
          ),
          div (className:= "col push-s3 s3") (
            // button if the user is not friends yet
            Option.when(props.requester.id != props.target.id) {
              ButtonComponent("Add", "/assets/img/add-user.svg", false, addFriend)
            }
          )
        )
      )
    }

    private def view(user: User) =
      window.location.href = userRoute + user.username

    private def addFriend() = {
      FetchJson.post(friendRequestRoute(props.target.id)) {user: User => ()}
    }

  }

}

