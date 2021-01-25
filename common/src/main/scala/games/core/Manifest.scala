package games.core

import games._
import models.User
import games.core.Game.AnyGame

object Manifest {
  
  final val Games = List[AnyGame](
    new TicTacToe(0),
    new Chess(1)
  )

  final val Users = List[User] (
    new User(0, "Alice", ""),
    new User(1, "Bob", ""),
    new User(2, "Sneaky Joe", ""),
    new User(3, "Tiptaco", ""),
    new User(4, "Coconut", ""),
    new User(5, "Banana", ""),
    new User(6, "Trumpf", ""),
    new User(7, "Leo", ""),
    new User(8, "Leprechaun", ""),
    new User(9, "Gazelle", ""),
    new User(10, "Howdy", ""),
    new User(11, "Phoneafriend", ""),
    new User(12, "BigDog99", "")
  )
}