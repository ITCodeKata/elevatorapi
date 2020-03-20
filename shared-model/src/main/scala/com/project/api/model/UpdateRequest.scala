package com.project.api.model

import io.circe.generic.JsonCodec

@JsonCodec
case class UpdateRequest (elevatorID: Int,
                          floorNumber: Int,
                          goalFloorNumber: Int)
