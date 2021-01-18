package models.protocols

object SearchProtocol {
  
  case class SearchQuery[F, O] (
    filter: Seq[F],
    ordering: O,
    page: Int,
    pageSize: Int = 6
  )

  case class SearchResponse[T] (
    items: Seq[T],
    page: Int,
    pages: Int
  )
}