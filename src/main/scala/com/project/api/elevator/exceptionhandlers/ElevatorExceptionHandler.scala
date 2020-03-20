package com.project.api.elevator.exceptionhandlers

import akka.http.scaladsl.model.{HttpEntity, HttpResponse, MediaTypes, StatusCodes, headers}
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.ExceptionHandler
import com.project.api.elevator.exception.ElevatorException
import com.project.api.model.{ServiceResponse, Error, ErrorCodes}
import com.typesafe.scalalogging.LazyLogging
import io.circe.syntax._

object ElevatorExceptionHandler extends LazyLogging {

  private val loggerName = logger.underlying.getName

  def exceptionHandle = ExceptionHandler {

    case ex: ElevatorException => {
      val uuid = java.util.UUID.randomUUID()
      val res = ServiceResponse[Unit](
        isSuccess = false,
        data = None,
        error = Some(Error(StatusCodes.InternalServerError.intValue, ex.getMessage))
      )
      val entityResponse = HttpEntity(MediaTypes.`application/json`, res.asJson.noSpaces)
      ErrorCodes.ElevatorAPIFailed.code

      complete(
        HttpResponse(StatusCodes.InternalServerError, entity = entityResponse)
          .withHeaders(headers.RawHeader("Error-id", uuid.toString))
      )
    }
    case ex: Exception => {
      val uuid = java.util.UUID.randomUUID()
      val res = ServiceResponse[Unit](
        isSuccess = false,
        data = None,
        error = Some(Error(StatusCodes.InternalServerError.intValue, ex.getMessage))
      )
      val entityResponse = HttpEntity(MediaTypes.`application/json`, res.asJson.noSpaces)
      ErrorCodes.InternalServerError.code

      complete(
        HttpResponse(StatusCodes.InternalServerError, entity = entityResponse)
          .withHeaders(headers.RawHeader("Error-id", uuid.toString))
      )
    }

  }

}
