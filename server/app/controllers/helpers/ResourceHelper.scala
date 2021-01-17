package controllers.helpers

import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc.{AbstractController, Request, Result, AnyContent}

trait ResourceHelper {
    this: AbstractController =>

  protected implicit val ec: ExecutionContext

  protected def getOr404[T](o: Future[Option[T]])(f: T => Future[Result])
      (implicit request: Request[AnyContent]): Future[Result] = {
    
    o flatMap {
      case Some(value) => f(value)
      case None => Future.successful(NotFound(views.html.menus.notfound()))
    }
  }
}