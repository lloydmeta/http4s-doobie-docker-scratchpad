package com.beachape.persistence

import com.beachape.data.{NewTweet, TweetId}
import org.scalatest.{FunSpec, Matchers}
import doobie.scalatest.IOChecker
import helpers.DockerPostgresService

class QueryTypeSpec extends FunSpec with Matchers with IOChecker with DockerPostgresService {

  lazy val transactor = HikariOps.toTransactor(PostgresDBConfig).unsafeRunSync()

  override def beforeAll(): Unit = {
    super.beforeAll()
    Migration.withConfig(PostgresDBConfig).unsafeRunSync()
    ()
  }

  describe("TweetsAlg SQL Queries types") {

    it("should have the proper types for getting a tweet") {
      check(DoobieTweetsAlg.getTweetQuery(TweetId(10)))
    }
    it("should have the proper types for getting all tweets") {
      check(DoobieTweetsAlg.listTweetsQuery)
    }
    it("should have the proper types inserting a new Tweet") {
      check(DoobieTweetsAlg.insertTweetQuery(NewTweet("hello world")))
    }
    it("should have the proper types deleting a tweet") {
      check(DoobieTweetsAlg.deleteTweetQuery(TweetId(10)))
    }

  }

}
