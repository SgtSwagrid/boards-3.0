package models

import java.time.LocalDateTime

case class Friend (
  id: Int = -1,
  user1Id: Int,
  user2Id: Int,
  status: Int, 
  date: LocalDateTime,
) {
  def pending = status == 0
  def accepted = status == 1
  def declined = status == 2
}