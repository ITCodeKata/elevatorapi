package com.project.api.common.module

import akka.http.scaladsl.server.Directives.concat
import akka.http.scaladsl.server.Route
import com.project.api.common.service.Service

trait ServiceModule {

  protected def services: Set[Service]

  protected def getRouteFromServiceSet(services: Set[Service]): Route =
    if (services.isEmpty) {
      throw new Exception("Empty service set")
    } else if (services.size == 1) {
      services.head.route
    } else {
      val listServices = services.toList
      listServices.tail.foldLeft(listServices.head.route) { (b, a) ⇒
        concat(b, a.route)
      }
    }

  def route: Route = getRouteFromServiceSet(services)

}
