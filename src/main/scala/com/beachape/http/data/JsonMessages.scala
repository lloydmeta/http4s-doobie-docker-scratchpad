package com.beachape.http.data

final case class JsonError(message: String)           extends AnyVal
final case class JsonSuccess(message: Option[String]) extends AnyVal
