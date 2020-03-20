package com.project.api.elevator.repository

import com.project.api.model.Elevator

import scala.concurrent.Future

trait ElevatorRepo {

  def getElevatorsState(): Future[Seq[Elevator]]
  def updateElevatorsState(state:Future[Seq[Elevator]]): Future[Seq[Elevator]]
  def removeElevatorsState(): Future[Seq[Elevator]]
}
