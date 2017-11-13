package com.beachape.http.serdes
import cats.Applicative
import com.beachape.data.{NewTweet, Tweet, TweetId}
import com.beachape.persistence.TweetsAlg.InsertionError
import io.circe.Encoder
import io.circe.generic.semiauto._
import org.http4s.EntityEncoder
import org.http4s.circe._

object Encoders extends JsonEncoders with Encoders

trait Encoders {

  /**
    * The manually-written implicits are so that we don't have a circular implicit resolutin on EntityEncoder..
    */
  implicit def entityEncoderFromJsonEncoder[F[_], A: Encoder](
      implicit entityEncoder: EntityEncoder[F, String],
      fApplicative: Applicative[F],
      aEncoder: Encoder[A]): EntityEncoder[F, A] =
    jsonEncoderOf[F, A](entityEncoder, fApplicative, aEncoder)

}

trait JsonEncoders {

  implicit val tweetIdJsonEncoder: Encoder[TweetId] = Encoder.encodeLong.contramap(_.value)

  implicit val tweetJsonEncoder: Encoder[Tweet] = deriveEncoder[Tweet]

  implicit val newTweetJsonEncoder: Encoder[NewTweet] = deriveEncoder[NewTweet]

  implicit val jsonErrorJsonEncoder: Encoder[JsonError] = deriveEncoder[JsonError]

  implicit val jsonSuccessJsonEncoder: Encoder[JsonSuccess] = deriveEncoder[JsonSuccess]

  implicit val tweetInsertionErrorJsonEncoder: Encoder[InsertionError] =
    Encoder[JsonError].contramap {
      case InsertionError(t) =>
        val msg = Option(t.getMessage)
          .map(s => s"Could not insert Tweet due to: $s")
          .getOrElse("Could not insert Tweet")
        JsonError(msg)
    }

}

final case class JsonError(message: String)           extends AnyVal
final case class JsonSuccess(message: Option[String]) extends AnyVal
