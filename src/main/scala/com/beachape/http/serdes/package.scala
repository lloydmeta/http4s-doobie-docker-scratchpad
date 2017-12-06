package com.beachape.http

import cats.{Functor, Monad}
import org.http4s.{DecodeFailure, DecodeResult, EntityDecoder, MediaRange, Message}

package object serdes {

  /**
    * Enrichments for now until we get them in Http4s directly
    */
  implicit class RichEntityDecoder[F[_], T](val self: EntityDecoder[F, T]) extends AnyVal {

    def handleError(f: DecodeFailure => T)(implicit F: Functor[F]): EntityDecoder[F, T] =
      transform {
        case Left(e)      => Right(f(e))
        case r @ Right(_) => r
      }

    def handleErrorWith(f: DecodeFailure => DecodeResult[F, T])(
        implicit F: Monad[F]): EntityDecoder[F, T] = transformWith {
      case Left(e)  => f(e)
      case Right(r) => DecodeResult.success(r)
    }

    def bimap[T2](f: DecodeFailure => DecodeFailure, s: T => T2)(
        implicit F: Functor[F]): EntityDecoder[F, T2] =
      transform {
        case Left(e)  => Left(f(e))
        case Right(r) => Right(s(r))
      }

    def transform[T2](t: Either[DecodeFailure, T] => Either[DecodeFailure, T2])(
        implicit F: Functor[F]): EntityDecoder[F, T2] =
      new EntityDecoder[F, T2] {
        override def consumes: Set[MediaRange] = self.consumes

        override def decode(msg: Message[F], strict: Boolean): DecodeResult[F, T2] =
          self.decode(msg, strict).transform(t)
      }

    def biflatMap[T2](f: DecodeFailure => DecodeResult[F, T2], s: T => DecodeResult[F, T2])(
        implicit F: Monad[F]): EntityDecoder[F, T2] =
      transformWith {
        case Left(e)  => f(e)
        case Right(r) => s(r)
      }

    def transformWith[T2](f: Either[DecodeFailure, T] => DecodeResult[F, T2])(
        implicit F: Monad[F]): EntityDecoder[F, T2] =
      new EntityDecoder[F, T2] {
        override def consumes: Set[MediaRange] = self.consumes

        override def decode(msg: Message[F], strict: Boolean): DecodeResult[F, T2] =
          DecodeResult(
            F.flatMap(self.decode(msg, strict).value)(r => f(r).value)
          )
      }
  }

}
