package com.beachape.persistence

import cats.effect.IO
import com.beachape.data.{NewTweet, TweetId}
import helpers.H2DatabaseService
import org.scalatest.{FunSpec, Matchers}

class DoobieTweetAlgSpec extends FunSpec with Matchers with H2DatabaseService {

  lazy val alg = new DoobieTweetsAlg[IO](H2Transactor)

  describe("CRUD") {

    it("return work") {
      val io = for {
        initList <- alg.listTweets()
        _ = initList shouldBe 'empty
        impossibleGet <- alg.getTweet(TweetId(Long.MaxValue)) // Huge Id
        _ = impossibleGet shouldBe None
        insertedOrErr <- alg.insert(NewTweet("heyo"))
        Right(inserted) = insertedOrErr
        listAfterInsert <- alg.listTweets()
        _ = listAfterInsert should contain(inserted)
        updateCount <- alg.update(inserted.copy(message = "hi"))
        _ = updateCount shouldBe 1
        deleteCount <- alg.delete(inserted.id)
        _ = deleteCount shouldBe 1
        listAfterDelete <- alg.listTweets()
        _ = listAfterDelete shouldBe 'empty
        getAfterDelete <- alg.getTweet(inserted.id)
        _ = getAfterDelete shouldBe None
      } yield ()
      io.unsafeRunSync()

    }

  }

}
