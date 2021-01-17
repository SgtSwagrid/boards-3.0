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


object FriendsView {

  @JSExportTopLevel("profileFriends")
  def profileFriends() = {

    val allUsers = Manifest.Users
    val user = decode[User](document.getElementById("user").asInstanceOf[html.Input].value).toOption.get

    ReactDOM.render (
      // todo fix this to use the database
      MenuComponent(allUsers, user),
      document.getElementById("root")
    )
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

  case class Props(user: User, requester: User)

  def render() = span(
      className := "menu-item block grey darken-3 z-depth-2 waves-effect hoverable",
      onClick := (_ => view)
    ) (
      div(className := "col s6 left-align white-text medium-text") (props.user.username)
      ,
      // button if the user is not friends yet
      ButtonComponent("Add", "/assets/img/add-user.svg", addFriend)
    )

  private def view() = {
    // todo - view a user profile ?
  }

  private def addFriend() = {
    // todo
  }

}