/*
package com.project.api.elevator

import com.project.api.model.ElevatorState

import scala.collection.immutable.Queue

object test {
  object DIRECTION extends Enumeration {
    type DIRECTION = Value
    val NONE, UP, DOWN = Value
  }

  case class Elevator(elevatorID: Int, direction: DIRECTION.DIRECTION, floorNumber: Int, floorList: Vector[Int], floorQueue: Queue[Int])

  var availableElevators = initialiseElevator(16)


  def initialiseElevator(noOfActiveElevator: Int) ={
    val elevators = scala.collection.mutable.MutableList.tabulate(noOfActiveElevator)(n => {
      Elevator(
        elevatorID = n+1,
        direction = DIRECTION.NONE,
        floorNumber = 0,
        floorList = Vector.empty[Int],
        floorQueue = Queue.empty[Int]
      )
    })
    elevators
  }

  def update(elevatorID: Int,
             floorNumber: Int,
             goalFloorNumber: Int): Unit = {

    val thisElevator =  availableElevators.find(e => {
      e.elevatorID == elevatorID}) match  {
      case Some(elevator) => elevator // if elevator is found - great :)
      case None => throw  new  Exception("did not found elevator")
    }

    var elevatorDirection = DIRECTION.NONE
    if( thisElevator.floorQueue.isEmpty){
      elevatorDirection =  goalFloorNumber match {
        case gfn: Int if gfn > floorNumber =>  DIRECTION.UP
        case _ => DIRECTION.DOWN
      }
    } else {
      elevatorDirection = thisElevator.direction
    }

    var floorList = Vector.empty[Int]
    var floorQueue = Queue.empty[Int]

    if(elevatorDirection == DIRECTION.UP ){
      val (nextFloorList, laterFloorList) = thisElevator.floorList.partition(_ > floorNumber)
      if(goalFloorNumber > floorNumber){

        val nextNewFloorList = nextFloorList :+ goalFloorNumber
        val sortedNextNewFloorList = sortedList(nextNewFloorList, asc)
        floorList = sortedNextNewFloorList ++ laterFloorList
        floorQueue = sortedListToQ(sortedNextNewFloorList ++ laterFloorList)
      }
      else{
        val laterNewFloorList = laterFloorList :+ goalFloorNumber
        val sortedLaterNewFloorList = sortedList(laterNewFloorList,desc)
        floorList = nextFloorList ++ sortedLaterNewFloorList
        floorQueue = sortedListToQ(nextFloorList ++ sortedLaterNewFloorList)
      }
    }
    else if (elevatorDirection == DIRECTION.DOWN ){
      val (nextFloorList, laterFloorList) = thisElevator.floorList.partition(_ < floorNumber)
      if( goalFloorNumber < floorNumber){
        val nextNewFloorList = nextFloorList :+ goalFloorNumber
        val sortedNextNewFloorList = sortedList(nextNewFloorList, desc)
        floorList = sortedNextNewFloorList ++ laterFloorList
        floorQueue = sortedListToQ(sortedNextNewFloorList ++ laterFloorList)
      }
      else {
        val laterNewFloorList = laterFloorList :+ goalFloorNumber
        val sortedLaterNewFloorList = sortedList(laterNewFloorList,asc)
        floorList = nextFloorList ++ sortedLaterNewFloorList
        floorQueue = sortedListToQ(nextFloorList ++ sortedLaterNewFloorList)
      }

    }

    availableElevators  = availableElevators.map{e => {
      e.elevatorID match {
        case eId: Int if eId == elevatorID => {
          if (e.floorQueue.isEmpty){
            e.copy(direction = elevatorDirection,floorNumber = floorNumber, floorList = floorList, floorQueue = floorQueue)
          } else {
            e.copy(floorList = floorList, floorQueue = floorQueue)
          }
        }
        case _ => e
      }
    }}
    println(availableElevators)

    def sortedList(floorList:Vector[Int], f:(Int, Int) => Boolean):Vector[Int] = {
      floorList.sortWith(f)
    }

    def asc(x:Int, y:Int) = x < y
    def desc (x:Int, y:Int) = x > y

    def sortedListToQ[A](s: Vector[A]): Queue[A] = s.distinct match {
      case x +: xs => x +: sortedListToQ(xs)
      case _ => Queue.empty[A]
    }
  }

  def step(): Unit = {

    def moveToNextFloors(floorNumber:Int, goalFloorNumber: Int): Unit = {
      val num = goalFloorNumber match {
        case x: Int if x > floorNumber => 1
        case _ => -1
      }

      for (currentFloor <- floorNumber until goalFloorNumber by num) {
        println(s"---Current floor ${currentFloor}")
        Thread.sleep(1000)
      }
    }

    /*  @annotation.tailrec*/
    def processQueue(elevatorID: Int, floorNumber:Int, queue: collection.immutable.Queue[Int]): Unit = if (queue.nonEmpty) {
      val (goalFloorNumber, rest) = queue.dequeue
      println(s"*ElevatorID: ${elevatorID} Current Floor ${floorNumber} Going to goal floor number ${goalFloorNumber}")
      moveToNextFloors(floorNumber, goalFloorNumber)
      availableElevators = availableElevators.map { e => {
        e.elevatorID match {
          case eId: Int if eId == elevatorID => {
            e.copy(floorNumber = goalFloorNumber)
          }
          case _ => e
        }
      }
      }

      processQueue(elevatorID, goalFloorNumber, rest)
    }

    availableElevators.foreach(e => {
      e.floorQueue match {
        case q if !q.isEmpty => processQueue(e.elevatorID, e.floorNumber, q)
        case _ => println("No queue!. Please initialize pickup request")
      }
    })
  }

  /** Dispatches least busy elevator to requested floor */
  def pickup(floor: Int, direction: DIRECTION.DIRECTION) {
   val worker = availableElevators.find(
     e => e.direction match {
       case DIRECTION.NONE => true // if this elevator is not doing anything - take it
       case elevatorDirection => { // if elevator is on the move and moves in the same direction - take it
         val position = relativeDirection(e.floorNumber, floor)
         ((position == direction) && (direction == elevatorDirection))
       }
     }
   ) match {
     case Some(elevator) => elevator // if elevator is found - great :)
     case None => throw  new  Exception("")//levators.minBy(e => e.targetFloors.size) // else, return elevator with smalles numbers of targets
   }
    update(worker.elevatorID, worker.floorNumber, floor)
  }

  def status(): Seq[ElevatorState] = {
    val status = availableElevators.filter(e => !e.floorQueue.isEmpty).map( e => {
      ElevatorState(
        elevatorID = e.elevatorID,
        floorNumber = e.floorNumber,
        goalFloorNumber = e.floorQueue.front
      )
    })
    println(status)
    status
  }

  def relativeDirection(from: Int, to: Int) = {
    if (from < to) {
      DIRECTION.UP
    } else {
      DIRECTION.DOWN
    }
  }

  update(1,5, 3)
  update(1,5, 1)
  update(1,5, 4)
  update(1,5, 11)
  pickup(2,DIRECTION.UP)
  println(availableElevators)
  status()
  step()

  /*update(1,3, 4)
  update(1,3, 6)
  update(1,3, 6)
  pickup(14,DIRECTION.UP)
  update(1,3, 5)
  println(availableElevators)
  pickup(14,DIRECTION.DOWN)
  update(2,4, 5)
  update(1,3, 8)
  update(1,3, 11)
  update(1,3, 1)
  update(1,3, 2)
  println(availableElevators)
  status()
  step()*/

}




*/
