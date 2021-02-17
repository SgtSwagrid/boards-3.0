package games.core

import games.core.{Piece, Vec}

sealed trait Action

object Action {

  case class Place[V <: Vec](pos: V, piece: Piece) extends Action

  case class Move[V <: Vec](from: V, to: V) extends Action

  case class Destroy[V <: Vec](pos: V) extends Action

  case class Select[V <: Vec]
    (pos: V, options: Seq[SelectOption])
  extends Action
  
  abstract class SelectOption(name: String, texture: String)
}