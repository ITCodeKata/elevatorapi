package com.project.api.elevator

import cats.effect.{ContextShift, IO}
import com.project.api.common.elevator.ElevatorConfig
import com.project.api.common.service.Service
import com.project.api.common.util.Tags.{AppConfig, DbIOBound, FJExecContext}
import com.typesafe.config.Config
import com.project.api.common.util.Tags.{DbIOBound, FJExecContext}
import com.project.api.elevator.repository.{ElevatorRepo, ElevatorRepoImpl}
import com.project.api.elevator.routes.ElevatorRoute
import com.project.api.elevator.service.{ElevatorService, ElevatorServiceImpl}
import com.softwaremill.macwire.{Module, wire}
import com.softwaremill.tagging._

import scala.concurrent.ExecutionContext

@Module
class ElevatorModule(
     appConfig: Config @@ AppConfig
)(
    implicit protected val fjExecContext: ExecutionContext @@ FJExecContext,
    implicit val ctxDbIO: ContextShift[IO] @@ DbIOBound
) {

  implicit protected val elevatorConfig: ElevatorConfig = ElevatorConfig(appConfig)
  val elevatorRepoImpl: ElevatorRepo = wire[ElevatorRepoImpl]

  protected lazy val elevatorServiceImpl: ElevatorService = wire[ElevatorServiceImpl]

  lazy val routes: Service = wire[ElevatorRoute]
}
