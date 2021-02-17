package games.core

import scala.collection.decorators._
import scala.collection.immutable.MultiDict
import games.core.{Action, Piece, Vec}

case class State[V <: Vec, P <: Piece, S] (

  pieces: Map[V, P] = Map[V, P](),
  piecesByOwner: MultiDict[Int, V] = MultiDict[Int, V](),
  occurences: MultiDict[P, V] = MultiDict[P, V](),

  labels: MultiDict[V, State.Label] = MultiDict[V, State.Label](),
  labelOccurences: MultiDict[State.Label, V] = MultiDict[State.Label, V](),

  players: List[PlayerState[P]] = List[PlayerState[P]](),

  stage: S = null,
  turn: Int = 0,
  outcome: State.Outcome = State.Ongoing
) {

  def addPiece(pos: V, piece: P) = {

    val previous = pieces.get(pos)

    copy (

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
  }

  def addPieces(pieces: Iterable[(V, P)]): State[V, P, S] = {

    pieces.toList match {
      case (pos, piece) :: tail =>
        addPiece(pos, piece).addPieces(tail)
      case Nil => this
    }
  }

  def addPieces(pos: Seq[V], pieces: Seq[P]): State[V, P, S] = {
    addPieces(pos zip pieces)
  }

  def movePiece(from: V, to: V): State[V, P, S] = {
    removePiece(from).addPiece(to, pieces(from))
  }

  def removePiece(pos: V) = {

    val piece = pieces(pos)

    copy (
      pieces = pieces - pos,
      piecesByOwner = piecesByOwner - (piece.ownerId -> pos),
      occurences = occurences - (piece -> pos)
    )
  }

  def addLabel(pos: Seq[V], label: State.Label) = {

    copy (
      labels = labels concat pos.map(_ -> label),
      labelOccurences = labelOccurences concat pos.map(label -> _)
    )
  }

  def removeLabel(pos: V, label: State.Label) = {

    copy (
      labels = labels - (pos -> label),
      labelOccurences = labelOccurences - (label -> pos)
    )
  }

  def purgeLabel(label: State.Label) = {

    copy (
      labels = labelOccurences.get(label)
        .foldLeft(labels)((l, p) => l - (p -> label)),
      labelOccurences = labelOccurences -* label
    )
  }

  def clearLabels(pos: V) = {

    copy (
      labels = labels -* pos,
      labelOccurences = labels.get(pos)
        .foldLeft(labelOccurences)((p, l) => p - (l -> pos))
    )
  }

  def moveablePieces: Iterable[(V, Piece.Moveable[V, State[V, P, S]])] = {

    pieces filter {
      case (_, piece) =>
        piece.isInstanceOf[Piece.Moveable[_, _]]
    } map {
      case (pos, piece) =>
        (pos, piece.asInstanceOf[Piece.Moveable[V, State[V, P, S]]])
    }
  }

  def moves(playerId: Int): Seq[(Action.Move[V], State[V, P, S])] = {

    piecesByOwner.get(playerId).toSeq flatMap {
      pos => pieces(pos) match {

        case p: Piece.Moveable[_, _] => {
          val piece = p.asInstanceOf[Piece.Moveable[V, State[V, P, S]]]
          piece.moves(this, pos)
        }
        case _ => Nil
      }
    }
  }

  def checked(pos: V): Boolean = {

    val attackers = (0 until players.size)
      .filter(_ != pieces(pos).ownerId)
      .flatMap(piecesByOwner.get)

    attackers exists { from =>
      pieces(from) match {

        case p: Piece.Moveable[_, _] => {
          val piece = p.asInstanceOf[Piece.Moveable[V, State[V, P, S]]]
          piece.sight(this, from).contains(pos)
        }
        case _ => false
      }
    }
  }

  def mated(playerId: Int): Boolean = {
    moves(playerId).isEmpty
  }

  def endTurn(skip: Int = 1) = {
    copy(turn = (turn + skip) % players.size)
  }

  def endGame(outcome: State.Outcome) = {
    copy(outcome = outcome)
  }

  def withPlayers(numPlayers: Int) = {
    copy(players = List.fill(numPlayers)(PlayerState[P]()))
  }

  def byPlayer[T](options: T*) = options(turn)

  def piecesBy(f: Piece => Boolean) = {
    pieces filter { case _ -> piece => f(piece) }
  }

  def piecesByOwner(ownerId: Int) = {
    piecesBy(_.ownerId == ownerId)
  }

  def empty(pos: V) = !pieces.isDefinedAt(pos)
  def occupied(pos: V) = pieces.isDefinedAt(pos)
  def friendly(pos: V) = pieces.get(pos).exists(_.ownerId == turn)
  def enemy(pos: V) = pieces.get(pos).exists(_.ownerId != turn)

  def nextPlayerId(skip: Int = 1) = (turn + skip) % players.size
}

case class PlayerState[P <: Piece] (
  score: Int = 0,
  captures: Seq[P] = Seq()
)

object State {

  type AnyState = State[_ <: Vec, _ <: Piece, _]
  type VState[V <: Vec] = State[V, _ <: Piece, _]

  sealed trait Outcome
  case object Ongoing extends Outcome
  case class Winner(playerId: Int) extends Outcome
  case object Draw extends Outcome

  trait Label
}