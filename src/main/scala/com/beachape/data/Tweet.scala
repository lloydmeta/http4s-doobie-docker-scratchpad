package com.beachape.data

final case class TweetId(value: Long) extends AnyVal

final case class NewTweet(message: String) extends AnyVal

final case class Tweet(id: TweetId, message: String)
