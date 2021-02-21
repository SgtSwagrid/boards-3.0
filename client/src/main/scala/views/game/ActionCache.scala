package views.game

import scala.collection.decorators._
import scala.collection.immutable.MultiDict

import games.core.{Action, ActionSet, State, Piece, Vec}

class ActionCache[V <: Vec, P <: Piece]
    (actionSet: ActionSet[V, P, Action[V]]) {

  type A = Action[V]
  
  val actions = actionSet.actions.toSet
  val places = actionSet.places.toSet
  val moves = actionSet.moves.toSet
  val placesAt = places.map(a => a.pos -> a).to(MultiDict)
  val movesFrom = moves.map(a => a.from -> a).to(MultiDict)

  val successors: Seq[State[V, P]] = actionSet.successors.toSeq
  val actionSeq = successors.flatMap(_.action)
  
  def indexOf(action: A) = actionSeq.indexOf(action)
}