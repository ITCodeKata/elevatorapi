package com.project.api.model

import io.circe.generic.JsonCodec

import scala.collection.immutable.Queue

@JsonCodec
case class Elevator(elevatorID: Int, direction: ElevatorDirection.ElevatorDirection, floorNumber: Int, floorList: Vector[Int], floorQueue: Queue[Int])
