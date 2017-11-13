package com.beachape.persistence

import cats.effect.IO
import com.beachape.data.TweetId
import helpers.H2DatabaseService
import org.scalatest.{FunSpec, Matchers}

class DoobieTweetAlgSpec extends FunSpec with Matchers with H2DatabaseService {

  lazy val alg = new DoobieTweetsAlg[IO](H2Transactor)

  describe("CRD") {

    it("return work") {
      val io = for {
        initList <- alg.listTweets()
        _ = initList shouldBe 'empty
        impossibleGet <- alg.getTweet(TweetId(Long.MaxValue)) // Huge Id
        _ = impossibleGet shouldBe None
        // The rest of these don't work under H2 because we use RETURNING syntax in our SQL
//        insertedOrErr <- alg.insert(NewTweet("heyo"))
//        Right(inserted) = insertedOrErr
//        listAfterInsert <- alg.listTweets()
//        _ = listAfterInsert should contain(inserted)
//        deleteCount <- alg.delete(inserted.id)
//        _               = deleteCount shouldBe 1
//        listAfterDelete = alg.listTweets()
//        _               = listAfterDelete shouldBe 'empty
//        getAfterDelete <- alg.getTweet(inserted.id)
//        _ = getAfterDelete shouldBe None
      } yield ()
      io.unsafeRunSync()

    }

  }

}
