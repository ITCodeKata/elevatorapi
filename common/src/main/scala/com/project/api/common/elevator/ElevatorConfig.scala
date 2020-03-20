package com.project.api.common.elevator

import com.typesafe.config.Config

case class ElevatorConfig(elevatorCount: Int, direction: String, floorNumber: Int)

object ElevatorConfig {
  def apply(config: Config): ElevatorConfig = {
    val elevatorsConfig = config.getConfig("elevators")
    ElevatorConfig(
      elevatorCount = elevatorsConfig.getInt("elevator-count"),
      direction =  elevatorsConfig.getString("direction"),
      floorNumber = elevatorsConfig.getInt("initialFloorNumber")
    )
  }
}
