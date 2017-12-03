package com.beachape.http.responses

import cats.Monad
import com.beachape.http.data.JsonError
import com.beachape.http.serdes.Encoders._
import org.http4s.{DecodeFailure, HttpVersion, MessageFailure, Response, Status}

/**
  * Error responses that fail with a message and respond with Json
  */
abstract class JsonErrorsResponse extends MessageFailure {

  def status: Status

  def cause: Option[Throwable] = None

  def toHttpResponse[F[_]](httpVersion: HttpVersion)(implicit F: Monad[F]): F[Response[F]] = {
    Response[F](status, httpVersion)
      .withBody(JsonError(message))
  }
}

final case class InvalidMessageBodyFailure(receivedBody: String)
    extends JsonErrorsResponse
    with DecodeFailure {
  def status: Status = Status.UnprocessableEntity

  def message: String = s"Could not deserialise the following into a valid JSON : $receivedBody"
}
