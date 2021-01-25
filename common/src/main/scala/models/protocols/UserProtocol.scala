package models.protocols

object UserProtocol {
  
  sealed trait UserFilter
  
  case class NameContainsSubstring(substring: String) extends UserFilter
  case object NameAlphabetical extends UserFilter

}
