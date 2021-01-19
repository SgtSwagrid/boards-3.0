package games.core

case class Colour private (r: Int, g: Int, b: Int, hex: String)

object Colour {

  val red = rgb(255, 0, 0)
  val green = rgb(0, 255, 0)
  val blue = rgb(0, 0, 255)

  val white = rgb(255, 255, 255)
  val black = rgb(0, 0, 0)

  def rgb(r: Int, g: Int, b: Int) = Colour(r, g, b,
    (r * (1 << 16) + g * (1 << 8) + b).toHexString)

  def hex(hex: String) = {

    val hexInt = Integer.parseInt(hex.dropWhile(_ == '#'), 16)

    Colour (
      hexInt / (1 << 16),
      hexInt / (1 << 8) % (1 << 8),
      hexInt % (1 << 8),
      hex
    )
  }
}