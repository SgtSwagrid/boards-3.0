package views.menu

import slinky.core.StatelessComponent
import slinky.web.html._
import slinky.core.annotations.react
import slinky.core.facade.ReactElement

object Tabs {
  
  def apply(tabs: Tab*) = TabsComponent(tabs)

  case class Tab (
    name: String,
    icon: String,
    content: ReactElement
  )

  @react class TabsComponent extends StatelessComponent {
    
    case class Props(tabs: Seq[Tab])

    def render() = div (
      ul(className := "tabs tabs-fixed-width z-depth-1 blue-grey darken-3") (
        props.tabs.zipWithIndex map { case (tab, tabId) =>
          li(className := "tab") (a(className := "white-text", href := s"#$tabId") (
            img(className := "small-icon", src := tab.icon),
            b(tab.name)
          ))
        }
      ),
      props.tabs.zipWithIndex map { case (tab, tabId) =>
        div(id := tabId.toString) (
          tab.content
        )
      }
    )
  }
}