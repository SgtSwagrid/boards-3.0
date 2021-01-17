package views.components

import slinky.core.StatelessComponent
import slinky.web.html._
import slinky.core.annotations.react

@react class TabsComponent extends StatelessComponent {
  
  case class Props(tabs: Seq[(String, String)])

  def render() =
    ul(className := "tabs tabs-fixed-width z-depth-1 blue-grey darken-3") (
      props.tabs map { case (name, icon) =>
        li(className := "tab") (a(className := "white-text", href := "#") (
          img(className := "small-icon", src := icon),
          b(name)
        ))
      }
    )
}