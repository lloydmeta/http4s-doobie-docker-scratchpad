package com.beachape

import cats.effect.IO
import com.beachape.data.{NewTweet, Tweet}
import io.circe.Json
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfterAll, FunSpec, Matchers}
import org.http4s.{Method, Request, Uri}
import org.http4s.client.blaze._
import org.scalatest.OptionValues._

class AppServerSpec
    extends FunSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with BeforeAndAfterAll {

  private val serverAccess = sys.props.get("http4s-db-scratchpad:8080").value
  private val httpClient   = PooledHttp1Client[IO]()

  import com.beachape.http.serdes.Decoders._
  import com.beachape.http.serdes.Encoders._

  describe("tweets endpoints") {

    it("should work initially be empty when there are no tweets") {
      val result =
        httpClient
          .expect[Seq[Tweet]](uri("tweets"))
          .unsafeRunSync()
      result shouldBe 'empty
    }

    it("should do CRD properly when inserting and deleting a tweet") {
      val test = for {
        req <- IO.pure(
          Request[IO](Method.POST, uri("tweets"))
            .withBody(NewTweet("message")))
        tweet <- httpClient.expect[Tweet](req)
        tweets <- httpClient
          .expect[Seq[Tweet]](uri("tweets"))
        _ = tweets.size shouldBe 1
        _ = tweets should contain(tweet)
        retrieved <- httpClient.expect[Option[Tweet]](uri(s"tweets/${tweet.id.value}"))
        _ = tweet shouldBe retrieved.value
        _          <- httpClient.expect[Json](Request[IO](Method.DELETE, uri(s"tweets/${tweet.id.value}")))
        retrieved2 <- httpClient.expect[Option[Tweet]](uri(s"tweets/${tweet.id.value}"))
        _ = retrieved2 shouldBe None
      } yield ()
      test.unsafeRunSync()
    }

  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    httpClient.shutdown.unsafeRunSync()
  }

  private def uri(path: String): Uri = Uri.unsafeFromString(s"http://$serverAccess/$path")

}
