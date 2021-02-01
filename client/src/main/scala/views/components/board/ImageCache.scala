package views.components.board

import scala.collection.mutable
import org.scalajs.dom.document
import org.scalajs.dom.raw.HTMLImageElement

class ImageCache {
  
  private val images = mutable.Map[String, HTMLImageElement]()

  def image(name: String) = {

    if (images.contains(name)) {
      images(name)
    
    } else {

      val image = document.createElement("img")
        .asInstanceOf[HTMLImageElement]
      image.src = s"/assets/img/$name"

      images += name -> image
      image
    }
  }
}