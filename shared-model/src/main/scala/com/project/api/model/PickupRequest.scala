package com.project.api.model

import com.project.api.model.ElevatorDirection.ElevatorDirection
import io.circe.generic.JsonCodec

@JsonCodec
case class PickupRequest (pickupFloor: Int, direction: ElevatorDirection )
