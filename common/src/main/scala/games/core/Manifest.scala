package games.core

import games._
import models.User

object Manifest {
  
  final val Games = List[Game](
    new TicTacToe(0),
    new Chess(1),
    new Amazons(2)
  )
}