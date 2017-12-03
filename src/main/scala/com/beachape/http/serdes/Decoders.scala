package com.beachape.http.serdes

import cats.effect.Effect
import cats.implicits._
import com.beachape.data.{NewTweet, Tweet, TweetId}
import com.beachape.http.responses.InvalidMessageBodyFailure
import io.circe.Decoder
import io.circe.generic.semiauto._
import org.http4s.{DecodeFailure, DecodeResult, EntityDecoder, MediaType}
import org.http4s.circe._

object Decoders extends JsonDecoders with Decoders

trait Decoders {

  implicit def entityDecoderFromJsonDecoder[F[_]: Effect, A: Decoder]: EntityDecoder[F, A] = {
    def jsonErrorResponse = EntityDecoder.decodeBy[F, A](MediaType.`application/json`) { m =>
      DecodeResult.failure(m.bodyAsText.runFoldMonoid.map { receivedBody =>
        InvalidMessageBodyFailure(receivedBody): DecodeFailure
      })
    }
    jsonOf[F, A].orElse(jsonErrorResponse)
  }

}

trait JsonDecoders {

  implicit val tweetIdJsonDecoder: Decoder[TweetId] =
    Decoder.decodeLong.map(TweetId.apply)

  implicit val tweetJsonDecoder: Decoder[Tweet] = deriveDecoder[Tweet]

  implicit val newTweetJsonDecoder: Decoder[NewTweet] = deriveDecoder[NewTweet]

}
