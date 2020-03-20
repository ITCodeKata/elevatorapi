package com.project.api.swagger

import akka.http.scaladsl.server.Route
import com.project.api.common.service.AnonymousService
import com.github.swagger.akka.model.Info
import com.project.api.elevator.routes.ElevatorRoute

class SwaggerDocService extends SwaggerHttpUiService with AnonymousService {
  override val apiClasses: Set[Class[_]] = Set(
    classOf[ElevatorRoute]
  )

  override val info         = Info(title = "API")
  override def route: Route = super.routes
}
