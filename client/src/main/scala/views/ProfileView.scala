package views.components

import org.scalajs.dom._, org.scalajs.dom.html
import scala.scalajs.js.annotation.JSExportTopLevel
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import slinky.core.{Component, StatelessComponent}
import slinky.core.facade.ReactElement
import slinky.core.annotations.react
import slinky.web.ReactDOM, slinky.web.html._
import models.User, games.core._
import java.awt.MenuComponent
import models.protocols.BoardProtocol._
import models.protocols.UserProtocol._
import slinky.web.svg.result
import slinky.web.svg.requiredExtensions
import models.protocols.SearchProtocol.SearchResponse
import models.protocols.SearchProtocol.SearchQuery
import views.helpers.FetchJson
import views.menu.{PaginationComponent, ButtonComponent, SearchComponent}
import views.menu.Tabs, views.menu.Tabs.Tab
import slinky.core.facade.ErrorBoundaryInfo
import cats.instances.boolean


object ProfileView {

  private val userQueryRoute = "/users/query"
  private def userViewRoute(username: String) = s"/users/profile/$username"
  private def friendRequestRoute(userId: Int) = s"/users/friends/create/$userId"
  private def friendAcceptRoute(userId: Int) = s"/users/friends/accept/$userId"
  private def friendDeclineRoute(userId: Int) = s"/users/friends/decline/$userId"

  @JSExportTopLevel("profileView")
  def profile() = {

    val allUsers = Manifest.Users
    val user = decode[User](document.getElementById("user")
      .asInstanceOf[html.Input].value).toOption.get
    val profileUser = decode[User](document.getElementById("profileUser")
      .asInstanceOf[html.Input].value).toOption.get

    ReactDOM.render (
      BrowseComponent(user, profileUser),
      document.getElementById("root")
    )
  }

  @react class BrowseComponent extends Component {

    case class Props(user: User, profileUser: User)
    case class State(tab: Int)
    def initialState = State(0)

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
          
          div (
            Option.when (userOwnsProfile) {
                FriendRequestsComponent(props.user)
            },
            //br(),
            FriendsComponent(props.user, props.profileUser),
            //br(),
            Option.when (userOwnsProfile) {
              SearchFriendsComponent(props.user)
            } 
          )
        )

      val boardsTab: Tab = Tab (
        if (userOwnsProfile) {"My Boards"} else {"Boards"},
        "/assets/img/menu.svg",
        views.BrowseView.BoardListComponent(UserBoards(props.profileUser.id))
      )

      div(className := "container") (
        Tabs (
          Seq(profileTab, friendsTab, boardsTab) :_*
        )
      )
    }    
  }

  @react class FriendRequestsComponent extends Component {

    case class Props(user: User)
    case class State(result: Option[SearchResponse[User]])
    def initialState: State = State(None)

    def render() = {
        div () (
          state.result map { result => div (

            Option.when(result.items.nonEmpty) {
              div(className := "large-text white-text") ("Pending Requests")
            },

            result.items map { user =>
              div (className := "container") (
                FriendRequestComponent(props.user, user, (_ => updateList()))
              )
            },

            Option.when(result.items.nonEmpty) { br() }
            
          )}
        )
    }

    private def updateList(): Unit = query(0)

    override def componentDidMount(): Unit = query(0)

    private def query(page: Int) = {
      val filter: UserFilter = PendingFriends(props.user.id)
      val query = SearchQuery(Seq(filter), page)

      FetchJson.postJson(userQueryRoute, query) {
        result: SearchResponse[User] => setState(state.copy(result = Some(result)))
      }
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

  @react class SearchFriendsComponent extends Component {

    /* Component to search and add new friends from. */ 

    case class Props(user: User)
    case class State(searchUsername: String)
    def initialState = State("")

    def render() = {
      div(
        SearchComponent("Search Users to Add", search _),
        Option.when(state.searchUsername != "") {
          UserListComponent(NameContainsSubstring(state.searchUsername), props.user, true)
        } 
      )
    }

    private def search(username: String) = {
        setState(state.copy(searchUsername = username))
    }
  }

  @react class UserListComponent extends Component {

    case class Props(filter: UserFilter, requester: User, canAdd: Boolean)
    case class State(result: Option[SearchResponse[User]])
    def initialState: State = State(None)

    def render() = {
      state.result map { result => div(className := "container") (
        result.items map { user =>
          UserComponent(props.requester, user, props.canAdd)
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

  @react class FriendsComponent extends StatelessComponent {

    /* Component to list the friends a user currently has. WIP. */ 

    case class Props(user: User, profileUser: User)

    def render() = {
      div(
        div(className := "large-text white-text") ("Friends"),
        div(UserListComponent(AcceptedFriends(props.user.id), props.user, false))
      )
    }
  }

  @react class FriendRequestComponent extends StatelessComponent {

    /* Display an open friend request to the current profile user */ 

    case class Props(user: User, requestee: User, resolve: Unit => Unit) 

    def render() = {
      div (className := "row-item") (
        span (className := "mid-text white-text medium-text") 
          (s"${props.requestee.username}"),

        span (className := "right span-btn") 
          (ButtonComponent("Deny", "/assets/img/cancel.svg", false, decline)),

        span (className := "right span-btn") 
          (ButtonComponent("Accept", "/assets/img/check.svg", false, accept))
        
      )
    }

    private def view() =
      window.location.href = userViewRoute(props.requestee.username)

    private def accept() = {
      FetchJson.post(friendAcceptRoute(props.requestee.id)) {success: Boolean => 
        if (success) props.resolve()
      }
    }
      
    private def decline() = {
      FetchJson.post(friendDeclineRoute(props.requestee.id)) {success: Boolean => 
        if (success) props.resolve()
      }
    }
  }

  @react class UserComponent extends StatelessComponent {

    /* Display the user a row in the list with clickable buttons. If the user is already friends
      there is no button to add them as a friend, Instead a check mark is shown.*/

    case class Props(requester: User, target: User, isFriend: Boolean = false)

    def render() = {
      div (className := "row-item") (
        span (className := "mid-text white-text medium-text") 
          (s"${props.target.username}"),

        span (className := "right span-btn") 
          (ButtonComponent("View", "/assets/img/user.svg", false, view)),

        span (className := "right span-btn") 
          (Option.when(props.isFriend) {
            ButtonComponent("Add", "/assets/img/add-user.svg", false, addFriend)
          })
      )
    }

    private def view() =
      window.location.href = userViewRoute(props.target.username)

    private def addFriend() = {
      FetchJson.post(friendRequestRoute(props.target.id)) {user: User => ()}
    }

  }

}

