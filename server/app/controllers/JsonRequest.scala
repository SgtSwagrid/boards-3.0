package controllers

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc._
import play.api.libs.json._

trait JsonRequest { this: AbstractController =>
  
  protected def withJson[A](f: A => Future[Result])
      (implicit request: Request[AnyContent], reads: Reads[A]): Future[Result] = {
    
    val json = Json.parse(request.body.asFormUrlEncoded.get("json")(0))
    Json.fromJson[A](json) match {
      case JsSuccess(a, _) => f(a)
      case JsError(_) => Future.successful(NotFound)
    }
  }
}