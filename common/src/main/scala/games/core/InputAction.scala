package games.core

import games.core.{Piece, Vec}

sealed trait InputAction

object InputAction {

  case class Place[V <: Vec](pos: V, piece: Piece) extends InputAction
  case class Move[V <: Vec](from: V, to: V) extends InputAction
  case class Destroy[V <: Vec](pos: V) extends InputAction

  case class Select[V <: Vec]
    (pos: V, options: Seq[SelectOption])
  extends InputAction
  
  abstract class SelectOption(name: String, texture: String)
}