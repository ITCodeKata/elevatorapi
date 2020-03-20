package com.project.api.elevator.service
import com.project.api.common.util.Tags.FJExecContext
import com.project.api.elevator.repository.ElevatorRepo
import com.project.api.model
import com.project.api.model.{Elevator, ElevatorDirection, ElevatorState}
import com.project.api.model.ElevatorDirection.ElevatorDirection
import com.softwaremill.tagging.@@
import com.typesafe.scalalogging.LazyLogging
import scala.collection.immutable.Queue
import scala.concurrent.{ExecutionContext, Future}

class ElevatorServiceImpl(repo: ElevatorRepo)(
    implicit protected val fjExecContext: ExecutionContext @@ FJExecContext
) extends ElevatorService
    with LazyLogging {

  /**
    *
    * @return
    */
  override def status(): Future[Seq[ElevatorState]] = {
    for {
      availableElevators: Seq[Elevator] <- repo.getElevatorsState()
    } yield {
      availableElevators.filter(f => !f.floorQueue.isEmpty).map { availE =>
        ElevatorState(
          elevatorID = availE.elevatorID,
          floorNumber = availE.floorNumber,
          goalFloorNumber = availE.floorQueue.front
        )
      }
    }
  }

  /**
    *
    * @param elevatorID
    * @param floorNumber
    * @param goalFloorNumber
    * @return
    */
  override def update(elevatorID: Int, floorNumber: Int, goalFloorNumber: Int): Future[Unit] = {
    for {
      availableElevators: Seq[Elevator] <- repo.getElevatorsState()
    } yield {
      val thisElevator: Elevator = availableElevators.find { e =>
        e.elevatorID == elevatorID
      } match {
        case Some(elevator) => elevator
        case None => throw new Exception("did not found elevator")
      }

      val elevatorDirection = thisElevator.floorQueue match {
        case x: Queue[Int] if x.isEmpty => getDirection(goalFloorNumber, floorNumber)
        case _ => {
          thisElevator.direction
        }
      }

      def constructElevatorQueue = {
        val result: (Vector[Int], Queue[Int]) = elevatorDirection match {
          case ElevatorDirection.UP => {
            val (nextFloorList, laterFloorList) = thisElevator.floorList.partition(_ > floorNumber)
            if (goalFloorNumber > floorNumber) {
              val nextNewFloorList = nextFloorList :+ goalFloorNumber
              val sortedNextNewFloorList = sortedList(nextNewFloorList, asc)
              val floorList = sortedNextNewFloorList ++ laterFloorList
              val floorQueue = sortedListToQ(sortedNextNewFloorList ++ laterFloorList)
              (floorList, floorQueue)
            } else {
              val laterNewFloorList = laterFloorList :+ goalFloorNumber
              val sortedLaterNewFloorList = sortedList(laterNewFloorList, desc)
              val floorList = nextFloorList ++ sortedLaterNewFloorList
              val floorQueue = sortedListToQ(nextFloorList ++ sortedLaterNewFloorList)
              (floorList, floorQueue)
            }
          }
          case ElevatorDirection.DOWN => {
            val (nextFloorList, laterFloorList) = thisElevator.floorList.partition(_ < floorNumber)
            if (goalFloorNumber < floorNumber) {
              val nextNewFloorList = nextFloorList :+ goalFloorNumber
              val sortedNextNewFloorList = sortedList(nextNewFloorList, desc)
              val floorList = sortedNextNewFloorList ++ laterFloorList
              val floorQueue = sortedListToQ(sortedNextNewFloorList ++ laterFloorList)
              (floorList, floorQueue)
            } else {
              val laterNewFloorList = laterFloorList :+ goalFloorNumber
              val sortedLaterNewFloorList = sortedList(laterNewFloorList, asc)
              val floorList = nextFloorList ++ sortedLaterNewFloorList
              val floorQueue = sortedListToQ(nextFloorList ++ sortedLaterNewFloorList)
              (floorList, floorQueue)
            }
          }
        }
        result
      }

      def updateElevatorState(elevatorID: Int, floorNumber: Int, availableElevators: Seq[Elevator], elevatorDirection: ElevatorDirection, floorList: Vector[Int], floorQueue: Queue[Int]) = {
        val availableElevator = availableElevators.map { e =>
          e.elevatorID match {
            case eId: Int if eId == elevatorID => {
              if (e.floorQueue.isEmpty) {
                e.copy(
                  direction = elevatorDirection,
                  floorNumber = floorNumber,
                  floorList = floorList,
                  floorQueue = floorQueue
                )
              } else {
                e.copy(floorList = floorList, floorQueue = floorQueue)
              }
            }
            case _ => e
          }
        }
        availableElevator
      }

      val result: (scala.Vector[Int], _root_.scala.collection.immutable.Queue[Int]) = constructElevatorQueue

      val (floorList, floorQueue) = result
      val availableElevator = updateElevatorState(elevatorID, floorNumber, availableElevators, elevatorDirection, floorList, floorQueue)
      repo.updateElevatorsState(Future(availableElevator))
    }
  }

  /**
    *
    * @param floor
    * @param direction
    * @return
    */
  override def pickup(floor: Int, direction: ElevatorDirection): Future[Unit] = {
    for {
      availableElevators: Seq[Elevator] <- repo.getElevatorsState()
    } yield {
      val pickupElevator = availableElevators.find(e =>
        e.direction match {
          case ElevatorDirection.NONE => true
          case elevatorDirection => {
            val position = relativeDirection(e.floorNumber, floor)
            ((position == direction) && (direction == elevatorDirection))
          }
        }
      ) match {
        case Some(elevator) => elevator
        case None =>
          throw new Exception("Did not find any elevator")
      }
      update(pickupElevator.elevatorID, pickupElevator.floorNumber, floor)
    }
  }

  /**
    * Update the state and floor in loop
    * @return
    */
  override def step(): Future[Unit] = {

    def moveToNextFloors(elevatorID: Int, floorNumber: Int, goalFloorNumber: Int): Unit = {
      val num = goalFloorNumber match {
        case x: Int if x > floorNumber => 1
        case _ => -1
      }

      for (currentFloor <- floorNumber to goalFloorNumber by num) {
        for {
          availableElevators: Seq[Elevator] <- repo.getElevatorsState()
        } yield {
          val updatedElevatorsState = availableElevators.map { e =>
            e.elevatorID match {
              case eId: Int if eId == elevatorID => {
                e.copy(floorNumber = currentFloor)
              }
              case _ => e
            }
          }
          repo.updateElevatorsState(Future(updatedElevatorsState))
        }
        Thread.sleep(200)
        println(s"---Current floor $currentFloor")
      }

    }

    def processQueue(elevatorID: Int, floorNumber: Int, queue: Queue[Int]): Unit = {
      if (queue.nonEmpty) {
        val (goalFloorNumber, rest) = queue.dequeue
        println(s"*ElevatorID: $elevatorID Current Floor $floorNumber Going to goal floor number $goalFloorNumber")
        moveToNextFloors(elevatorID, floorNumber, goalFloorNumber)

        for{
          availableElevators: Seq[Elevator] <- repo.getElevatorsState()
        } yield {
          val updatedElevatorsState = availableElevators.map { e: Elevator =>
            e.elevatorID match {
              case eId: Int if eId == elevatorID => {
                e.copy(floorNumber = goalFloorNumber)
              }
              case _ => e
            }
          }
          repo.updateElevatorsState(Future(updatedElevatorsState))
        }

        processQueue(elevatorID, goalFloorNumber, rest)
      }
    }

    for {
      availableElevators: Seq[Elevator] <- repo.getElevatorsState()
    } yield {
      availableElevators.foreach { e =>
        e.floorQueue match {
          case q if !q.isEmpty => processQueue(e.elevatorID, e.floorNumber, q)
          case _               => ()
        }
      }
    }
  }

  /**
    *
    * @return
    */
  override def reset(): Future[Unit] = {
    for {
      _ <- repo.removeElevatorsState()
    } yield ()
  }


  private def asc(x: Int, y: Int)  = x < y
  private def desc(x: Int, y: Int) = x > y

  private def getDirection(goalFloorNumber: Int, floorNumber: Int): model.ElevatorDirection.Value = {
    if (goalFloorNumber > floorNumber) {
      ElevatorDirection.UP
    } else {
      ElevatorDirection.DOWN
    }
  }

  private def relativeDirection(from: Int, to: Int): model.ElevatorDirection.Value = {
    if (from < to) {
      ElevatorDirection.UP
    } else {
      ElevatorDirection.DOWN
    }
  }

  private def sortedList(floorList: Vector[Int], f: (Int, Int) => Boolean): Vector[Int] = floorList.sortWith(f)

  private def sortedListToQ[A](s: Vector[A]): Queue[A] = s.distinct match {
    case x +: xs => x +: sortedListToQ(xs)
    case _       => Queue.empty[A]
  }

}
