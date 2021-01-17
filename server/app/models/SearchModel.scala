package models

import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.MySQLProfile.api._
import protocols.SearchProtocol._

class SearchModel(db: Database)(implicit ec: ExecutionContext) {
  
  def paginate[U](items: Query[_, U, Seq], query: SearchQuery):
      Future[SearchResponse[U]] = {

    val visible = items
      .drop(query.page * query.pageSize)
      .take(query.pageSize).result

    val pages = items.length.result map { len =>
      (len + query.pageSize-1) / query.pageSize
    }

    db.run(visible zip pages) map {
      case (visible, pages) =>
        SearchResponse(visible, query.page, pages max 1)
    }
  }
}