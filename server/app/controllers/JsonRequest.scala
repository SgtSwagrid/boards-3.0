package controllers

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc.{AbstractController, Result, Request, AnyContent}
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

trait JsonRequest { this: AbstractController =>
  
  protected def withJson[A](f: A => Future[Result])
      (implicit request: Request[AnyContent], decoder: Decoder[A]): Future[Result] = {
    
    decode[A](request.body.asFormUrlEncoded.get("json")(0)) match {
      case Right(a) => f(a)
      case Left(_) => Future.successful(NotFound)
    }
  }
}