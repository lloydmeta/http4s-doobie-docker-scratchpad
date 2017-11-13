package com.beachape.http.services

import cats.effect.IO
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.headers.Location

@SuppressWarnings(Array("org.wartremover.warts.Throw"))
object Index {

  implicit val uriQueryParamEncode: QueryParamEncoder[Uri] {
    def encode(value: Uri): QueryParameterValue
  } = new QueryParamEncoder[Uri] {
    override def encode(value: Uri) =
      QueryParameterValue(value.toString)
  }
  val tweetsSwaggerPath: Uri = uri("/tweets/swagger-spec.json")

  val service: HttpService[IO] = HttpService[IO] {
    case GET -> Root => {
      TemporaryRedirect(
        Location(uri("/assets/swagger-ui/3.2.2/index.html")
          .withQueryParam("url", tweetsSwaggerPath)))
    }
  }

}
