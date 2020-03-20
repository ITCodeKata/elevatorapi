package com.project.api.elevator.routes

import com.project.api.elevator.repository.ElevatorRepo
import com.project.api.elevator.service.{ ElevatorServiceImpl}
import com.project.api.common.util.Tags.FJExecContext
import com.project.api.model.{Elevator, ElevatorDirection, ElevatorState}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import akka.http.scaladsl.testkit.Specs2RouteTest
import scala.collection.immutable.Queue
import com.softwaremill.tagging.{@@, _}
import org.mockito.Mockito.when
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import scala.concurrent.ExecutionContext.Implicits
import scala.concurrent.{ExecutionContext, Future}

class ElevatorServiceImplSpec extends Specification with Mockito with Specs2RouteTest with FailFastCirceSupport {

  trait Setup extends Scope {
    implicit val executor: ExecutionContext @@ FJExecContext = Implicits.global.taggedWith[FJExecContext]

    val repo=  mock[ElevatorRepo]
    val elevatorServiceImpl       = new ElevatorServiceImpl(repo)(executor)
  }

  "ElevatorServiceImpl status method" should {
    "return success response" in new Setup {
      val expected = Seq(ElevatorState(
        elevatorID = 1,
        floorNumber = 1,
        goalFloorNumber = 2)
      )
      val request= Seq(Elevator(
        elevatorID = 1,
        direction = ElevatorDirection.UP,
        floorNumber = 1,
        floorList = Vector[Int](2, 3, 4),
        floorQueue= Queue[Int](2, 3, 4)
      ))

      when(repo.getElevatorsState()).thenReturn(Future.successful(request))

      val response = elevatorServiceImpl.status()
      there was one(repo).getElevatorsState()
    }

  }
}
