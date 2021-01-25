package models

import java.time.LocalDateTime

case class Friendship (
  id: Int = -1,
  userId1: Int,
  userId2: Int,
  status: Int, 
  date: LocalDateTime,
) {
  def pending = status == 0
  def accepted = status == 1
  def declined = status == 2
}