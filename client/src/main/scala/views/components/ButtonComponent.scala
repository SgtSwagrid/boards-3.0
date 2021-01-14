package views.components

import slinky.core.StatelessComponent
import slinky.web.html._
import slinky.core.annotations.react

@react class ButtonComponent extends StatelessComponent {

  private val buttonClass = className := "btn block yellow darken-2 waves-effect"
  private val textClass = className := "left blue-grey-text text-darken-4"
  private val imageClass = className := "btn-icon blue-grey darken-3"
  
  case class Props(text: String, image: String, click: () => Unit)

  def render() = a(buttonClass, onClick := (_ => props.click())) ( span (
    b(textClass)(props.text),
    img(imageClass, src := props.image)
  ))
}