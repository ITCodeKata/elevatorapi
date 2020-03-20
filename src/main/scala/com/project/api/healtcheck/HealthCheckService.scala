package com.project.api.healtcheck

import akka.http.scaladsl.server.Directives.{complete, path, _}
import akka.http.scaladsl.server.Route
import com.project.api.common.service.AnonymousService
import com.project.api.common.util.Tags.AppConfig
import com.softwaremill.tagging.@@
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import scala.io.Source
import scala.util.Try
import scala.util.control.NonFatal
class HealthCheckService(appConfig: Config @@ AppConfig) extends AnonymousService with FailFastCirceSupport {

  override def route: Route = concat(
    path("healthcheck")(complete("OK")),
    path("online") {
      handleOnline()
    },
    path("online.txt") {
      handleOnline()
    }
  )

  def handleOnline(): Route = {
    val message = Try {
      val readmeText = Source.fromResource("online.txt").mkString
      readmeText
    }.recover {
      case NonFatal(e) => e.getMessage
    }
    complete(message)
  }

}
