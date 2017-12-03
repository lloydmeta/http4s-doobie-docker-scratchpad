package com.beachape.http.services

import cats.effect.IO
import com.beachape.config.SwaggerConf
import com.beachape.data.{NewTweet, Tweet, TweetId}
import com.beachape.http.data.{JsonError, JsonSuccess}
import com.beachape.persistence.services.TweetsAlg
import doobie.util.transactor.Transactor
import io.circe.Json
import org.http4s.{EntityDecoder, HttpService}
import org.http4s.dsl.io._

object Tweets {

  import com.beachape.http.serdes.Encoders._
  import com.beachape.http.serdes.Decoders._

  object TweetIdVar {
    def unapply(s: String): Option[TweetId] = LongVar.unapply(s).map(TweetId.apply)
  }

  def service(transactor: Transactor[IO], swaggerConf: SwaggerConf): HttpService[IO] = {
    val dao         = TweetsAlg.doobie[IO](transactor)
    val swaggerSpec = swagger(swaggerConf)
    HttpService[IO] {
      case GET -> Root =>
        Ok(dao.listTweets())
      case GET -> Root / TweetIdVar(tweetId) =>
        dao.getTweet(tweetId).flatMap {
          case Some(tweet) => Ok(tweet)
          case None        => NotFound(JsonError(s"No tweet with id $tweetId"))
        }
      case req @ POST -> Root => {
        req.decodeWith(EntityDecoder[IO, NewTweet], strict = true) { newTweet =>
          dao.insert(newTweet).flatMap {
            case Right(tweet)    => Created(tweet)
            case Left(insertErr) => BadRequest(insertErr)
          }
        }
      }
      case req @ PATCH -> Root =>
        req.decodeWith(EntityDecoder[IO, Tweet], strict = true) { tweet =>
          dao.update(tweet).flatMap {
            case 1 => Ok(tweet)
            case _ => BadRequest(JsonError("Update failed"))
          }
        }
      case DELETE -> Root / TweetIdVar(tweetId) =>
        dao.delete(tweetId).flatMap { count =>
          if (count > 0)
            Ok(JsonSuccess(Some(s"Deleted $count rows.")))
          else
            NotFound(JsonError(s"No such tweet was found by $tweetId"))
        }
      case GET -> Root / "swagger-spec.json" => Ok(swaggerSpec)
    }
  }

  // TODO: Use Rho to do this when Rho supports http4s 0.18+
  private def swagger(swaggerConf: SwaggerConf): Json = Json.obj(
    "swagger" -> Json.fromString("2.0"),
    "info" -> Json.obj(
      "description" -> Json.fromString("Simple Rest API for string tweets, etc")
    ),
    "host"     -> Json.fromString(swaggerConf.host),
    "basePath" -> Json.fromString("/tweets"),
    "schemes"  -> Json.arr(swaggerConf.schemes.map(Json.fromString): _*),
    "paths" -> Json.obj(
      "" -> Json.obj(
        "get" -> Json.obj(
          "summary"     -> Json.fromString("Get a list of tweets"),
          "description" -> Json.fromString(""),
          "operationId" -> Json.fromString("listTweets"),
          "consumes"    -> Json.arr(Json.fromString("application/json")),
          "produces"    -> Json.arr(Json.fromString("application/json")),
          "parameters"  -> Json.arr(),
          "responses" -> Json.obj("200" -> Json.obj(
            "description" -> Json.fromString("Success"),
            "schema" -> Json.obj(
              "type" -> Json.fromString("array"),
              "items" -> Json.obj(
                "$ref" -> Json.fromString("#/definitions/Tweet")
              )
            ),
          ))
        ),
        "post" -> Json.obj(
          "summary"     -> Json.fromString("Create a tweet"),
          "description" -> Json.fromString(""),
          "operationId" -> Json.fromString("createTweet"),
          "consumes"    -> Json.arr(Json.fromString("application/json")),
          "produces"    -> Json.arr(Json.fromString("application/json")),
          "parameters" -> Json.arr(Json.obj(
            "in"          -> Json.fromString("body"),
            "name"        -> Json.fromString("body"),
            "description" -> Json.fromString("NewTweet object"),
            "required"    -> Json.fromBoolean(true),
            "schema" -> Json.obj(
              "$ref" -> Json.fromString("#/definitions/NewTweet")
            )
          )),
          "responses" -> Json.obj("201" -> Json.obj(
            "description" -> Json.fromString("Success"),
            "schema" -> Json.obj(
              "$ref" -> Json.fromString("#/definitions/Tweet")
            ),
            "400" -> Json.obj(
              "description" -> Json.fromString("Creation error"),
            )
          ))
        ),
        "patch" -> Json.obj(
          "summary"     -> Json.fromString("Update a tweet"),
          "description" -> Json.fromString(""),
          "operationId" -> Json.fromString("updateTweet"),
          "consumes"    -> Json.arr(Json.fromString("application/json")),
          "produces"    -> Json.arr(Json.fromString("application/json")),
          "parameters" -> Json.arr(Json.obj(
            "in"          -> Json.fromString("body"),
            "name"        -> Json.fromString("body"),
            "description" -> Json.fromString("Tweet object"),
            "required"    -> Json.fromBoolean(true),
            "schema" -> Json.obj(
              "$ref" -> Json.fromString("#/definitions/Tweet")
            )
          )),
          "responses" -> Json.obj("200" -> Json.obj(
            "description" -> Json.fromString("Success"),
            "schema" -> Json.obj(
              "$ref" -> Json.fromString("#/definitions/Tweet")
            ),
            "400" -> Json.obj(
              "description" -> Json.fromString("Update error"),
            )
          ))
        ),
      ),
      "{tweetId}" -> Json.obj(
        "get" -> Json.obj(
          "summary"     -> Json.fromString("Get single tweet"),
          "description" -> Json.fromString(""),
          "operationId" -> Json.fromString("getTweet"),
          "consumes"    -> Json.arr(Json.fromString("application/json")),
          "produces"    -> Json.arr(Json.fromString("application/json")),
          "parameters" -> Json.arr(Json.obj(
            "in"          -> Json.fromString("path"),
            "name"        -> Json.fromString("tweetId"),
            "description" -> Json.fromString("Id of the tweet."),
            "type"        -> Json.fromString("integer"),
            "format"      -> Json.fromString("int64"),
            "required"    -> Json.fromBoolean(true),
          )),
          "responses" -> Json.obj("200" -> Json.obj(
            "description" -> Json.fromString("Success"),
            "schema" -> Json.obj(
              "$ref" -> Json.fromString("#/definitions/Tweet")
            ),
            "404" -> Json.obj(
              "description" -> Json.fromString("Not found"),
            ),
          ))
        ),
        "delete" -> Json.obj(
          "summary"     -> Json.fromString("Deleta a tweet"),
          "description" -> Json.fromString(""),
          "operationId" -> Json.fromString("deleteTweet"),
          "consumes"    -> Json.arr(Json.fromString("application/json")),
          "produces"    -> Json.arr(Json.fromString("application/json")),
          "parameters" -> Json.arr(Json.obj(
            "in"          -> Json.fromString("path"),
            "name"        -> Json.fromString("tweetId"),
            "description" -> Json.fromString("Id of the tweet."),
            "type"        -> Json.fromString("integer"),
            "format"      -> Json.fromString("int64"),
            "required"    -> Json.fromBoolean(true),
          )),
          "responses" -> Json.obj("200" -> Json.obj(
            "description" -> Json.fromString("Success"),
            "schema" -> Json.obj(
              "$ref" -> Json.fromString("#/definitions/Tweet")
            ),
            "404" -> Json.obj(
              "description" -> Json.fromString("Not found"),
            ),
          ))
        ),
      ),
    ),
    "definitions" -> Json.obj(
      "Tweet" -> Json.obj(
        "type"     -> Json.fromString("object"),
        "required" -> Json.arr(Json.fromString("id"), Json.fromString("message")),
        "properties" -> Json.obj(
          "id" -> Json.obj(
            "type"   -> Json.fromString("integer"),
            "format" -> Json.fromString("int64"),
          ),
          "message" -> Json.obj(
            "type" -> Json.fromString("string")
          ),
        )
      ),
      "NewTweet" -> Json.obj(
        "type"     -> Json.fromString("object"),
        "required" -> Json.arr(Json.fromString("message")),
        "properties" -> Json.obj(
          "message" -> Json.obj(
            "type" -> Json.fromString("string")
          ))
      )
    )
  )

}
