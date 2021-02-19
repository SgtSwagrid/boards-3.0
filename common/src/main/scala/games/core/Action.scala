package games.core

import games.core.{Piece, Vec}

sealed trait Action[V <: Vec]

object Action {

  case class Place[V <: Vec](pos: V, piece: Piece) extends Action[V]

  case class Move[V <: Vec](from: V, to: V) extends Action[V]

  case class Destroy[V <: Vec](pos: V) extends Action[V]

  case class Select[V <: Vec]
    (pos: V, options: Seq[SelectOption])
  extends Action[V]
  
  abstract class SelectOption(name: String, texture: String)
}