package com.project.api.model

import akka.http.scaladsl.model.StatusCode
import io.circe.generic.JsonCodec

@JsonCodec
case class Error(code: Int, message: String)

case class CEGServiceException(error: Error) extends Exception(error.toString) {

  def int2StatusCode(): StatusCode = StatusCode.int2StatusCode(error.code / 10)
}

object ErrorCodes {

  val UnexpectedError: Error            = Error(5001, "Unexpected Error while Handling the request.")
  val InternalServerError: Error        = Error(5002, "Internal Server Error while Handling the request.")
  val ElevatorAPIFailed: Error = Error(8001, "Elevator Operation Failed")
}
