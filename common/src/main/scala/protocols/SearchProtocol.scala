package protocols

object SearchProtocol {
  
  case class SearchQuery (
    page: Int,
    pageSize: Int = 6
  )

  case class SearchResponse[T] (
    items: Seq[T],
    page: Int,
    pages: Int
  )
}