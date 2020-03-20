package com.project.api.model

import io.circe.{Decoder, Encoder}

object ElevatorDirection extends Enumeration {
  type ElevatorDirection = Value
  val NONE = Value
  val UP = Value
  val DOWN = Value

  implicit val decoder: Decoder[ElevatorDirection.Value] = Decoder.enumDecoder(ElevatorDirection)
  implicit val encoder: Encoder[ElevatorDirection.Value] = Encoder.enumEncoder(ElevatorDirection)

  def withNameOpt(s: String): Option[Value] = values.find(_.toString == s)
}