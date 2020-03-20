package com.project.api.elevator.repository

import cats.effect.{ContextShift, IO}
import com.project.api.common.elevator.ElevatorConfig
import com.project.api.common.util.Tags.{DbIOBound, FJExecContext}
import com.softwaremill.tagging.@@
import com.typesafe.scalalogging.LazyLogging

import scala.collection.immutable.Queue
import scala.concurrent.{ExecutionContext, Future}
import akka.actor.ActorSystem
import akka.http.caching.LfuCache
import akka.http.caching.scaladsl.Cache
import akka.stream.ActorMaterializer
import com.project.api.model.{Elevator, ElevatorDirection}

import scala.concurrent.ExecutionContextExecutor


class ElevatorRepoImpl(
    implicit protected val fjExecContext: ExecutionContext @@ FJExecContext,
    ctxDbIO: ContextShift[IO] @@ DbIOBound,
    implicit val elevatorConfig: ElevatorConfig,

) extends ElevatorRepo
    with LazyLogging {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  var cache: Cache[String, Seq[Elevator]] = LfuCache[String,  Seq[Elevator]]
  val ELEVATORSTATE: String = "elevatorState"

  def initialiseElevators(): Future[List[Elevator]] ={
    val elevators = List.tabulate(elevatorConfig.elevatorCount)(n => {
      Elevator(
        elevatorID = n+1,
        direction = elevatorConfig.direction match {
          case "none" => ElevatorDirection.NONE
          case "up" => ElevatorDirection.UP
          case "down" => ElevatorDirection.DOWN
          case _ => ElevatorDirection.NONE
        },
        floorNumber = elevatorConfig.floorNumber,
        floorList = Vector.empty[Int],
        floorQueue = Queue.empty[Int]
      )
    })
    Future.successful(elevators)
  }

  def getElevatorsState(): Future[Seq[Elevator]] = {
    cache.getOrLoad(ELEVATORSTATE, _ => initialiseElevators())
  }

  def updateElevatorsState(state:Future[Seq[Elevator]]): Future[Seq[Elevator]] = {
    cache.remove(ELEVATORSTATE)
    cache.getOrLoad(ELEVATORSTATE, _ => state)
  }

  def removeElevatorsState(): Future[Seq[Elevator]] = {
    cache.remove(ELEVATORSTATE)
    cache.getOrLoad(ELEVATORSTATE, _ => initialiseElevators())
  }

}
