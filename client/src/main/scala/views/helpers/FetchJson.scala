package views.helpers

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalajs.dom.{document, html, FormData}
import org.scalajs.dom.ext.Ajax
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

object FetchJson {
  
  private val csrfToken = document.getElementById("csrfToken")
    .asInstanceOf[html.Input].value

  private val headers = Map (
    "Content-Type" -> "application/json",
    "Csrf-Token" -> csrfToken
  )

  def getJson[B](url: String)(success: B => Unit)
      (implicit decoder: Decoder[B]): Unit = {
    
    Ajax.get(url) map { data =>
      decode[B](data.responseText)
        .foreach(success)
    }
  }

  def postJson[A, B](url: String, data: A)(success: B => Unit)
      (implicit encoder: Encoder[A], decoder: Decoder[B]): Unit = {

    val json = data.asJson.toString

    Ajax.post(url, json, headers=headers) map { data =>
      decode[B](data.responseText)
        .foreach(success)
    }
  }

  def post[B](url: String)(success: B => Unit)
      (implicit decoder: Decoder[B]): Unit = {
    
    Ajax.post(url, headers=headers) map { data =>
      decode[B](data.responseText)
        .foreach(success)
    }
  }
}