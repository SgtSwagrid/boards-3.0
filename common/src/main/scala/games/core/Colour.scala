package games.core

case class Colour private (hex: String)

object Colour {

  val black = hex("#000000")
  val red = hex("#FF0000")
  val green = hex("#00FF00")
  val blue = hex("#0000FF")
  val yellow = hex("#FFFF00")
  val purple = hex("#FF00FF")
  val cyan = hex("#00FFFF")
  val white = hex("#FFFFFF")

  // American Palette
  val greenDarnerTail = hex("#74B9FF")
  val electronBlue = hex("#0984E3")
  val sourLemon = hex("#FFEAA7")
  val brightYarrow = hex("#FDBC6E")

  // British Palette
  val lynxWhite = hex("#F5F6FA")
  val hintOfPensive = hex("#DCDDE1")

  def rgb(r: Int, g: Int, b: Int) = {
    val hex = (r * (1 << 16) + g * (1 << 8) + b)
    Colour("#" + hex.toHexString.reverse.padTo(6, "0").reverse.mkString)
  }

  def hex(hex: String) = Colour(hex)
}