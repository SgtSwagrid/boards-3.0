package games.core

import scala.collection.decorators._
import scala.collection.immutable.MultiDict
import games.core.{Action, Piece, Vec}

case class State[V <: Vec, P <: Piece] (

  pieces: Map[V, P] = Map[V, P](),
  piecesByOwner: MultiDict[Int, V] = MultiDict[Int, V](),
  occurences: MultiDict[P, V] = MultiDict[P, V](),

  labels: MultiDict[V, State.Label] = MultiDict[V, State.Label](),
  labelOccurences: MultiDict[State.Label, V] = MultiDict[State.Label, V](),

  players: List[PlayerState[P]] = List[PlayerState[P]](),

  stage: Int = 0,
  turn: Int = 0,
  ply: Int = 0,
  outcome: State.Outcome = State.Ongoing,

  previous: Option[State[V, P]] = None,
  action: Option[Action[V]] = None
) {

  def addPiece(pos: V, piece: P): State[V, P] = {

    val previous = pieces.get(pos)

    val newState = copy (

      pieces = pieces + (pos -> piece),

      piecesByOwner = previous match {
        case Some(previous) => (piecesByOwner
          - (previous.ownerId -> pos)
          + (piece.ownerId -> pos))
        case None => piecesByOwner + (piece.ownerId -> pos)
      },
      
      occurences = previous match {
        case Some(previous) => occurences - (previous -> pos) + (piece -> pos)
        case None => occurences + (piece -> pos)
      }
    )
    
    newState.addLabel(pos, State.Modified)
  }

  def addPieces(pieces: Iterable[(V, P)]): State[V, P] = {

    pieces.toList match {
      case (pos, piece) :: tail =>
        addPiece(pos, piece).addPieces(tail)
      case Nil => this
    }
  }

  def addPieces(pos: Seq[V], pieces: Seq[P]): State[V, P] = {
    addPieces(pos zip pieces)
  }

  def movePiece(from: V, to: V): State[V, P] = {
    removePiece(from).addPiece(to, pieces(from))
  }

  def removePiece(pos: V): State[V, P] = {

    val newState = pieces.get(pos) match {

      case Some(piece) => copy (
        pieces = pieces - pos,
        piecesByOwner = piecesByOwner - (piece.ownerId -> pos),
        occurences = occurences - (piece -> pos)
      )

      case None => this
    }
    
    newState.addLabel(pos, State.Modified)
  }

  def addLabel(pos: V, label: State.Label): State[V, P] = {

    copy (
      labels = labels + (pos -> label),
      labelOccurences = labelOccurences + (label -> pos)
    )
  }

  def addLabels(pos: Seq[V], label: State.Label): State[V, P] = {

    copy (
      labels = labels concat pos.map(_ -> label),
      labelOccurences = labelOccurences concat pos.map(label -> _)
    )
  }

  def removeLabel(pos: V, label: State.Label): State[V, P] = {

    copy (
      labels = labels - (pos -> label),
      labelOccurences = labelOccurences - (label -> pos)
    )
  }

  def purgeLabel(label: State.Label): State[V, P] = {

    copy (
      labels = labelOccurences.get(label)
        .foldLeft(labels)((l, p) => l - (p -> label)),
      labelOccurences = labelOccurences -* label
    )
  }

  def clearLabels(pos: V): State[V, P] = {

    copy (
      labels = labels -* pos,
      labelOccurences = labels.get(pos)
        .foldLeft(labelOccurences)((p, l) => p - (l -> pos))
    )
  }

  def endStage(skip: Int = 1): State[V, P] = {
    copy(stage = stage + skip)
  }

  def endTurn(skip: Int = 1): State[V, P] = {
    copy(turn = (turn + skip) % players.size, stage = 0)
  }

  def endStageOrTurn(maxStages: Int = 0): State[V, P] = {
    val nextStage = stage + 1
    if (nextStage < maxStages)
      copy(turn = turn, stage = nextStage)
    else
      copy(turn = (turn + 1) % players.size, stage = 0)
  }

  def nextTurn(skip: Int = 1) = (turn + skip) % players.size

  def endGame(outcome: State.Outcome): State[V, P] = {
    copy(outcome = outcome)
  }

  def withPlayers(numPlayers: Int): State[V, P] = {
    copy(players = List.fill(numPlayers)(PlayerState[P]()))
  }

  def byPlayer[T](options: T*) = options(turn)

  def empty(pos: V) = !pieces.isDefinedAt(pos)
  def occupied(pos: V) = pieces.isDefinedAt(pos)

  def friendly(pos: V, player: Int = turn) =
    pieces.get(pos).exists(_.ownerId == player)

  def enemy(pos: V, player: Int = turn) =
    pieces.get(pos).exists(_.ownerId != player)

  def allEmpty(pos: Iterable[V]) = !pos.exists(pieces.isDefinedAt)

  def isPiece(pos: V, pieceType: Class[_ <: P]): Boolean = {
    pieces.get(pos).exists(pieceType.isInstance)
  }

  def modified(pos: V): Boolean = {
    labels.get(pos).contains(State.Modified)
  }

  def start: State[V, P] = purgeLabel(State.Modified)
  def nextState(skip: Int = 1, stages: Int = 0) = (stage + skip) % stages

  def history: Seq[State[V, P]] = {
    this +: previous.toSeq.flatMap(_.history)
  }

  def actionsThisTurn: Seq[Action[V]] = {
    action.toSeq ++
      previous.filter(p => p.previous.exists(_.turn == p.turn))
        .toSeq.flatMap(_.actionsThisTurn)
  }
}

case class PlayerState[P <: Piece] (
  score: Int = 0,
  captures: Seq[P] = Seq()
)

object State {

  type AnyState = State[_ <: Vec, _ <: Piece]

  sealed trait Outcome
  case object Ongoing extends Outcome
  case class Winner(playerId: Int) extends Outcome
  case object Draw extends Outcome

  trait Label
  case object Modified extends Label

  trait Stage
}