package com.project.api.common.service

import akka.http.scaladsl.server.Route
import io.circe.Printer

trait Service {

  implicit lazy val circePrinter: Printer = Printer.noSpaces.copy(dropNullValues = true)

  def route: Route

}
