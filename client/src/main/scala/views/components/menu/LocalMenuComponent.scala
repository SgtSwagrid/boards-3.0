package views.components.menu

import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

@react class LocalMenuComponent extends Component {
  
  case class Props (items: List[MenuItem], pageSize: Int = 6)
  case class State(visible: List[MenuItem], search: String, page: Int)
  def initialState = State(props.items, "", 0)

  def render() = div (
    SearchComponent("Search", search _),
    state.visible.map(_.component),
    PaginationComponent(state.page, pages, goto _)
  )

  private def visible(search: String, page: Int) =
    props.items
      .filter(_.tags.exists(_.toLowerCase.contains(search)))
      .drop(page * props.pageSize).take(props.pageSize)

  private def search(search: String) =
    setState(state.copy (
      visible = visible(search.toLowerCase, 0),
      search = search.toLowerCase,
      page = 0
    ))

  private def goto(page: Int) =
    setState(state.copy (
      visible = visible(state.search, page),
      page = page
    ))

  private def pages =
    ((state.visible.size-1) / props.pageSize + 1) max 1
}