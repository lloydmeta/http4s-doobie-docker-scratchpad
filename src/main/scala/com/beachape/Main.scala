package com.beachape

import cats.effect.IO
import com.beachape.config.AppConf
import fs2.Stream
import org.http4s.server.blaze._
import org.http4s.util.StreamApp
import org.http4s.util.ExitCode
import org.http4s.server.staticcontent.{MemoryCache, WebjarService, webjarService}
import com.beachape.http.services.{Index, Tweets}
import com.beachape.persistence.{HikariOps, Migration}

object Main extends StreamApp[IO] {

  override def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] = {
    for {
      s      <- Stream.eval(BlazeProps().map(_.serve))
      sInner <- s
    } yield sInner
  }

}

@SuppressWarnings(Array("org.wartremover.warts.Throw"))
object BlazeProps {

  def apply(): IO[BlazeBuilder[IO]] =
    for {
      appConfigEither <- IO(AppConf.load())
      appConfig = appConfigEither match {
        case Right(c) => c
        case Left(err) => {
          throw new IllegalStateException(s"Could not load AppConfig: ${err.toList.mkString("\n")}")
        }
      }
      dbConfig = appConfig.db
      _  <- Migration.withConfig(dbConfig)
      xa <- HikariOps.toTransactor(dbConfig)
    } yield
      BlazeBuilder[IO]
        .bindHttp(appConfig.server.httpPort, "0.0.0.0")
        .mountService(Index.service, "/")
        .mountService(Tweets.service(xa, appConfig.swagger), "/tweets")
        // http://localhost/assets/swagger-ui/3.2.2/index.html
        .mountService(webjarService[IO](WebjarService.Config(cacheStrategy = MemoryCache[IO])),
                      "/assets")

}
