package views.menu

import slinky.core.StatelessComponent
import slinky.web.html._
import slinky.core.annotations.react

@react class ButtonComponent extends StatelessComponent {

  private val buttonClass = "btn yellow darken-2 waves-effect"
  private val blockClass = "btn block yellow darken-2 waves-effect"
  private val textClass = "left blue-grey-text text-darken-4"
  private val imageClass = "btn-icon blue-grey darken-3"
  
  case class Props (
    text: String,
    image: String,
    block: Boolean,
    click: () => Unit
  )

  def render() = span (
    a(className := (if (props.block) blockClass else buttonClass),
        onClick := (_ => props.click())) ( span (
      b(className := textClass)(props.text),
      img(className := imageClass, src := props.image)
    ))
  )
}