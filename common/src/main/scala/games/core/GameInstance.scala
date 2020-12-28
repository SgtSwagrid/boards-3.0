package games.core

import games.core.States._
import games.core.Pieces._
import games.core.Actions._
import games.core.Coordinates._

case class GameInstance (
  id: String,
  game: Game,
  state: AnyState,
  isPublic: Boolean,
  status: Int,
  players: List[Player],
  rematchBoardId: String,
  parentBoardId: String,
  parentStateId: Int
)