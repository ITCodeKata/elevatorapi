package com.project.api

import akka.actor.{ActorSystem, Scheduler}
import akka.stream.Materializer
import cats.effect.{ContextShift, IO, Timer}
import com.project.api.common.module.ServiceModule
import com.project.api.common.service.Service
import com.project.api.common.util.Tags
import com.project.api.common.util.Tags.{AppConfig, DbIOBound, FJExecContext, IOExecContext, RootConfig}
import com.project.api.healtcheck.HealthCheckService
import com.project.api.swagger.SwaggerDocService
import Tags._
import com.project.api.elevator.ElevatorModule
import com.softwaremill.macwire.{wire, wireSet}
import com.softwaremill.tagging._
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext
// $COVERAGE-OFF$
class MainServiceModule(
    appConfig: Config @@ AppConfig,
    rootConfig: Config @@ RootConfig,
)(
    implicit
    system: ActorSystem,
    mat: Materializer,
    scheduler: Scheduler,
    ioExecContext: ExecutionContext @@ IOExecContext,
    fjExecContext: ExecutionContext @@ FJExecContext,
    dbExecContext: ExecutionContext @@ DbIOBound,
    ctxBlckIOBound: ContextShift[IO] @@ IOExecContext,
    ctxDbIOBound: ContextShift[IO] @@ DbIOBound,
    timer: Timer[IO]
) extends ServiceModule {

  // Anonymous Services

  protected lazy val healthCheckService = new HealthCheckService(appConfig)

  protected lazy val swaggerService: SwaggerDocService = wire[SwaggerDocService]

  override protected lazy val services: Set[Service] = wireSet[Service]

  protected lazy val elevatorModule: ElevatorModule = wire[ElevatorModule]

  protected lazy val amendService: Service = elevatorModule.routes

}
// $COVERAGE-ON$
