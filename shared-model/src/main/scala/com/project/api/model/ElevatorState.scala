package com.project.api.model

import io.circe.generic.JsonCodec

@JsonCodec
case class ElevatorState(
                          elevatorID: Int,
                          floorNumber: Int,
                          goalFloorNumber: Int
                        )
