package views.menu

import slinky.core.StatelessComponent
import slinky.core.annotations.react
import slinky.web.html._

@react class SearchComponent extends StatelessComponent {
  
  case class Props(label: String, search: String => Unit)

  def render() = div(className := "input-field") (
    input(className := "white-text", id := "search", `type` := "text", autoFocus,
      onChange := (e => props.search(e.target.value))
    ),
    label(className := "white-text", htmlFor := "search") (props.label)
  )
}