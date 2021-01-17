package views.components

import slinky.core.StatelessComponent
import slinky.web.html._
import slinky.core.annotations.react

@react class PaginationComponent extends StatelessComponent {

  case class Props(page: Int, pages: Int, goto: Int => Unit)

  def render() = div(
    ul(className := "pagination center-align") (
      li(className := (if (props.page == 0) "disabled" else "waves-effect"),
          onClick := (_ => props.goto(props.page-1))) (
        a(className := (if (props.page == 0) "grey-text" else "white-text")) (
          i(className := "material-icons") ("chevron_left")
        )
      ),
      (0 until props.pages).map { page =>
        li(className := (if (props.page == page) "active yellow darken-2" else "waves-effect"),
          onClick := (_ => props.goto(page))) (
            a(className := (if (props.page == page) "black-text" else "white-text")) (page+1)
          )
      },
      li(className := (if (props.page == props.pages-1) "disabled" else "waves-effect"),
          onClick := (_ => props.goto(props.page+1))) (
        a(className := (if (props.page == props.pages-1) "grey-text" else "white-text")) (
          i(className := "material-icons") ("chevron_right")
        )
      )
    )
  )
}