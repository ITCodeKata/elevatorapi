package com.project.api.elevator.service

import com.project.api.model.ElevatorDirection.ElevatorDirection
import com.project.api.model.ElevatorState

import scala.concurrent.Future

trait ElevatorService {

  def status(): Future[Seq[ElevatorState]]
  def update(elevatorID: Int,
             floorNumber: Int,
             goalFloorNumber: Int):Future[Unit]
  /*def pickup(pickupRequest: PickupRequest)*/
  def pickup(floor: Int, direction: ElevatorDirection): Future[Unit]
  def step(): Future[Unit]
  def reset(): Future[Unit]
}
