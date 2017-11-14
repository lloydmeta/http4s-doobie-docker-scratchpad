package com.beachape.persistence

import cats.effect.Effect
import cats.implicits._
import com.beachape.data.{NewTweet, Tweet, TweetId}
import doobie.util.transactor.Transactor
import doobie._
import doobie.implicits._
import fs2.internal.NonFatal

object TweetsAlg {

  /**
    * Given a transactor, returns a Doobie-based implementation of Tweets algebra
    */
  def doobie[F[_]: Effect](xa: Transactor[F]) = new DoobieTweetsAlg[F](xa)

  sealed trait Error
  final case class InsertionError(underlying: Throwable) extends Error
}

/**
  * Our tweet persistence algebra
  */
abstract class TweetsAlg[F[_]: Effect] {

  import TweetsAlg._

  def getTweet(tweetId: TweetId): F[Option[Tweet]]

  def listTweets(): F[Seq[Tweet]]

  def insert(tweet: NewTweet): F[Either[InsertionError, Tweet]]

  def delete(tweetId: TweetId): F[Int]

  def update(tweet: Tweet): F[Int]

}

object DoobieTweetsAlg {

  implicit val tweetIdMeta: Meta[TweetId] = Meta.LongMeta.xmap(TweetId.apply, _.value)

  private[persistence] def getTweetQuery(tweetId: TweetId): Query0[Tweet] = {
    sql"""
      SELECT t.id, t.message
      FROM tweets as t
      WHERE t.id = $tweetId
      LIMIT 1
    """.query[Tweet]
  }

  private[persistence] val listTweetsQuery: Query0[Tweet] = {
    sql"""
      SELECT t.id, t.message
      FROM tweets as t
    """.query[Tweet]
  }

  private[persistence] def insertTweetQuery(newTweet: NewTweet): Update0 = {
    sql"""
       INSERT
       INTO tweets (message)
       VALUES (${newTweet.message})
       """.update
  }

  private[persistence] def deleteTweetQuery(tweetId: TweetId): Update0 = {
    sql"""
      DELETE FROM tweets as t
      WHERE t.id = $tweetId
    """.update
  }

  private[persistence] def updateTweetQuery(tweet: Tweet): Update0 = {
    sql"""
      UPDATE tweets
      SET message = ${tweet.message}
      WHERE id = ${tweet.id}
    """.update
  }

}

/**
  * Implementation of our algebra based o Doobie
  */
class DoobieTweetsAlg[F[_]: Effect](xa: Transactor[F]) extends TweetsAlg[F] {

  import DoobieTweetsAlg._
  import TweetsAlg._

  def getTweet(tweetId: TweetId): F[Option[Tweet]] = getTweetQuery(tweetId).option.transact(xa)

  def listTweets(): F[Seq[Tweet]] = listTweetsQuery.list.transact(xa).map(_.toSeq)

  def insert(newTweet: NewTweet): F[Either[InsertionError, Tweet]] =
    insertTweetQuery(newTweet)
      .withUniqueGeneratedKeys[TweetId]("id")
      .transact(xa)
      .map(id => Either.right[InsertionError, Tweet](Tweet(id, newTweet.message)))
      .recover {
        case NonFatal(e) => Either.left[InsertionError, Tweet](InsertionError(e))
      }

  def delete(tweetId: TweetId): F[Int] =
    deleteTweetQuery(tweetId).run.transact(xa)

  def update(tweet: Tweet): F[Int] =
    updateTweetQuery(tweet).run.transact(xa)
}
