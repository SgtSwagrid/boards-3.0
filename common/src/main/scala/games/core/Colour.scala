package games.core

case class Colour (hex: String) {

  private val hexInt = Integer.parseInt(hex.substring(1), 16)

  def r = hexInt / (1 << 16) % (1 << 8)
  def g = hexInt / (1 << 8) % (1 << 8)
  def b = hexInt % (1 << 8)

  def lighten(amount: Int) = Colour.rgb (
    0 max (r + amount) min 255,
    0 max (g + amount) min 255,
    0 max (b + amount) min 255
  )

  def darken(amount: Int) = lighten(-amount)
}

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
  val cityLights = hex("#DFE6E9")
  val electronBlue = hex("#0984E3")
  val soothingBreeze = hex("#B2BEC3")
  val sourLemon = hex("#FFEAA7")
  val brightYarrow = hex("#FDBC6E")

  // British Palette
  val lynxWhite = hex("#F5F6FA")
  val hintOfPensive = hex("#DCDDE1")

  // German Palette
  val fusionRed = hex("#FC5C65")
  val flirtatious = hex("#FED330")
  val nycTaxi = hex("#F7B731")
  val blueGrey = hex("#778CA3")
  val royalBlue = hex("#3867D6")
  val blueHorizon = hex("#4B6584")

  def rgb(r: Int, g: Int, b: Int) = {
    val hex = (r * (1 << 16) + g * (1 << 8) + b)
    Colour("#" + hex.toHexString.reverse.padTo(6, "0").reverse.mkString)
  }

  def hex(hex: String) = Colour(hex)
}