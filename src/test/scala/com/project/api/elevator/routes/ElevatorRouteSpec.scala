package com.project.api.elevator.routes

import com.project.api.common.util.Tags.FJExecContext
import com.project.api.elevator.service.ElevatorService
import com.project.api.model.{ElevatorDirection, ElevatorState, PickupRequest, UpdateRequest}
import com.softwaremill.tagging.@@
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.Specs2RouteTest
import com.project.api.model.ElevatorDirection.ElevatorDirection
import com.softwaremill.tagging.@@
import org.mockito.Mockito.when
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits
import com.softwaremill.tagging._

class ElevatorRouteSpec extends Specification with Mockito with Specs2RouteTest with FailFastCirceSupport {

  trait Setup extends Scope {
    implicit val executor: ExecutionContext @@ FJExecContext = Implicits.global.taggedWith[FJExecContext]

    val service=  mock[ElevatorService]
    val elevatorRoute       = new ElevatorRoute(service)(executor)
  }

  "Elevator update Route route /elevator/update" should {
    "return success response" in new Setup {
      val request = UpdateRequest (
        elevatorID = 1,
        floorNumber = 1,
        goalFloorNumber = 10)

      val expected: Future[Unit] = Future.successful(Unit)

      when(service.update(any[Int], any[Int], any[Int])).thenReturn(expected)

      Post(s"/elevator/update", request) ~> elevatorRoute.route ~> check {
        status shouldEqual StatusCodes.OK
      }

      there was one(service).update(any[Int], any[Int], any[Int])
    }

  }
  "Elevator pickup Route route" should {
    "/elevator/pickup return success response" in new Setup {
      val request = PickupRequest (
        pickupFloor = 1,
        direction = ElevatorDirection.UP)

      val expected: Future[Unit] = Future.successful(Unit)

      when(service.pickup(1, ElevatorDirection.UP)).thenReturn(expected)

      Post(s"/elevator/pickup", request) ~> elevatorRoute.route ~> check {
        status shouldEqual StatusCodes.OK
      }

      there was one(service).pickup(any[Int], any[ElevatorDirection])
    }
  }

  "Elevator status Route route" should {
    "/elevator/status return success response" in new Setup {
      val request = PickupRequest (
        pickupFloor = 1,
        direction = ElevatorDirection.UP)

      val expected: Seq[ElevatorState] = Seq(ElevatorState(
        elevatorID = 1,
        floorNumber = 1,
        goalFloorNumber = 15))

      when(service.status()).thenReturn(Future.successful(expected))

      Get(s"/elevator/status") ~> elevatorRoute.route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Seq[ElevatorState]] shouldEqual expected
      }
    }
  }
}
