package com.project.api.common.module

import akka.actor.{ActorSystem, Scheduler}
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.project.api.common.httpserver.HttpConfig
import com.project.api.common.util.Tags.{AppConfig, RootConfig}
import com.softwaremill.tagging.@@
import com.typesafe.config.Config

trait AppModule {

  def rootConfig: Config @@ RootConfig

  def appConfig: Config @@ AppConfig

  def httpConfig: HttpConfig = HttpConfig.unsafeReadFrom(appConfig)

  def system: ActorSystem

  def materializer: Materializer

  def scheduler: Scheduler

  def route: Route

}
