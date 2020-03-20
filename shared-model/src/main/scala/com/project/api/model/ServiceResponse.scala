package com.project.api.model

import io.circe.generic.JsonCodec

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

@JsonCodec
case class ServiceResponse[A](isSuccess: Boolean, data: Option[A], error: Option[Error])

object ServiceResponse {

  def fromFuture[A](fa: Future[A])(implicit ec: ExecutionContext): Future[ServiceResponse[A]] = {

    val promise = Promise[ServiceResponse[A]]
    fa.onComplete {
      case Success(r) ⇒ promise.success(ServiceResponse(isSuccess = true, Some(r), None))
      case Failure(exception) ⇒
        promise.success(
          ServiceResponse(
            isSuccess = false,
            None,
            error = Some(Error(ErrorCodes.UnexpectedError.code, exception.getMessage))
          )
        )
    }

    promise.future
  }

}
