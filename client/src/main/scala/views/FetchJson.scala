package views

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalajs.dom.{document, html, FormData}
import org.scalajs.dom.ext.Ajax
import play.api.libs.json._

object FetchJson {
  
  private val CsrfToken = document.getElementById("csrfToken")
    .asInstanceOf[html.Input].value

  private val headers = Map (
    "Content-Type" -> "application/json",
    "Csrf-Token" -> CsrfToken
  )

  def getJson[B](url: String)(success: B => Unit)
      (implicit reads: Reads[B]): Unit = {
    
    Ajax.get(url) map { data =>
      Json.fromJson[B](Json.parse(data.responseText)) match {
        case JsSuccess(b, _) => success(b)
        case JsError(_) => {}
      }
    }
  }

  def postJson[A, B](url: String, data: A)(success: B => Unit)
      (implicit writes: Writes[A], reads: Reads[B]): Unit = {

    val json = Json.toJson(data).toString

    Ajax.post(url, json, headers=headers) map { data =>
      Json.fromJson[B](Json.parse(data.responseText)) match {
        case JsSuccess(b, _) => success(b)
        case JsError(_) => {}
      }
    }
  }

  def post[B](url: String)(success: B => Unit)
      (implicit reads: Reads[B]): Unit = {
    
    Ajax.post(url, headers=headers) map { data =>
      Json.fromJson[B](Json.parse(data.responseText)) match {
        case JsSuccess(b, _) => success(b)
        case JsError(_) => {}
      }
    }
  }
}