package models.protocols

object UserProtocol {
  
  sealed trait UserFilter
  
  case class NameContainsSubstring(substring: String) extends UserFilter
  case object NameAlphabetical extends UserFilter

  case class PendingFriends(userId: Int) extends UserFilter
  case class AcceptedFriends(userId: Int) extends UserFilter

}
