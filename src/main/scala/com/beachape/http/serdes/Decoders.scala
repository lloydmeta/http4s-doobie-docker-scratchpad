package com.beachape.http.serdes

import cats.effect.Effect
import cats.implicits._
import com.beachape.data.{NewTweet, Tweet, TweetId}
import com.beachape.http.responses.InvalidMessageBodyFailure
import io.circe.Decoder
import io.circe.generic.semiauto._
import org.http4s.EntityDecoder
import org.http4s.circe._

object Decoders extends JsonDecoders with Decoders

trait Decoders {

  implicit def entityDecoderFromJsonDecoder[F[_]: Effect, A: Decoder]: EntityDecoder[F, A] =
    jsonOf[F, A].bimap(f => InvalidMessageBodyFailure(f.getMessage()), identity)

}

trait JsonDecoders {

  implicit val tweetIdJsonDecoder: Decoder[TweetId] =
    Decoder.decodeLong.map(TweetId.apply)

  implicit val tweetJsonDecoder: Decoder[Tweet] = deriveDecoder[Tweet]

  implicit val newTweetJsonDecoder: Decoder[NewTweet] = deriveDecoder[NewTweet]

}
