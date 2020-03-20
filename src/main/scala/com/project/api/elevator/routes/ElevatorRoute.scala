package com.project.api.elevator.routes

import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, handleExceptions, path, pathPrefix, post, _}
import akka.http.scaladsl.server.Route
import com.project.api.common.service.Service
import com.project.api.common.util.Tags.FJExecContext
import com.project.api.elevator.exceptionhandlers.ElevatorExceptionHandler
import com.project.api.elevator.service.ElevatorService
import com.project.api.model.{ElevatorDirection, ElevatorState, PickupRequest, UpdateRequest}
import com.softwaremill.tagging.@@
import io.circe.generic.auto._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.swagger.annotations.{Api, ApiImplicitParam, ApiImplicitParams, ApiOperation, ApiResponse, ApiResponses}
import javax.ws.rs.Path

import scala.concurrent.ExecutionContext

@Api(value = "/flight", consumes = "application/json", produces = "application/json")
@Path("/")
class ElevatorRoute(service: ElevatorService)(
    implicit protected val fjExecContext: ExecutionContext @@ FJExecContext
) extends Service
    with FailFastCirceSupport {

  override def route: Route =
    concat(
      handleExceptions(ElevatorExceptionHandler.exceptionHandle)(update),
      handleExceptions(ElevatorExceptionHandler.exceptionHandle)(pickup),
      handleExceptions(ElevatorExceptionHandler.exceptionHandle)(status),
      handleExceptions(ElevatorExceptionHandler.exceptionHandle)(step),
      handleExceptions(ElevatorExceptionHandler.exceptionHandle)(reset)
    )
  @Path("/elevator/update")
  @ApiOperation(httpMethod = "POST", value = "Elevators update")
  def update: Route = pathPrefix("elevator") {
    path("update") {
      post {
        entity(as[UpdateRequest]) { req =>
          complete(service.update(req.elevatorID,req.floorNumber, req.goalFloorNumber))
        }
      }
    }
  }

  @Path("/elevator/pickup")
  @ApiOperation(httpMethod = "POST", value = "Elevators pickup")
  def pickup: Route = pathPrefix("elevator") {
    path("pickup") {
      post {
        entity(as[PickupRequest]) { req =>
          val direction: ElevatorDirection.Value = ElevatorDirection
            .withNameOpt(req.direction.toString)
            .getOrElse(ElevatorDirection.NONE)
          complete(service.pickup(req.pickupFloor, direction))
        }
      }
    }
  }

  @Path("/elevator/status")
  @ApiOperation(httpMethod = "GET", value = "Get status of all elevators")
  @ApiResponses(
    Array(new ApiResponse(code = 200, message = "Successful response", response = classOf[Seq[ElevatorState]]))
  )
  def status: Route = pathPrefix("elevator") {
    path("status") {
      get {
        complete(service.status())
      }
    }
  }

  @Path("/elevator/step")
  @ApiOperation(httpMethod = "GET", value = "Step elevators")
  @ApiResponses(
    Array(new ApiResponse(code = 200, message = "Successful response", response = classOf[Seq[ElevatorState]]))
  )
  def step: Route = pathPrefix("elevator") {
    path("step") {
      get {
        complete(service.step())
      }
    }
  }

  @Path("/elevator/reset")
  @ApiOperation(httpMethod = "GET", value = "Step elevators")
  @ApiResponses(
    Array(new ApiResponse(code = 200, message = "Successful response", response = classOf[Seq[ElevatorState]]))
  )
  def reset: Route = pathPrefix("elevator") {
    path("reset") {
      get {
        complete(service.reset())
      }
    }
  }

}
