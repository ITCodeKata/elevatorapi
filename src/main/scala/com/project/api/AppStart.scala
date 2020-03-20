package com.project.api

import com.project.api.common.httpserver.HttpServer

object AppStart extends App {

  val appModule = new MainAppModule
  import appModule._
  HttpServer(httpConfig, route, rootConfig)

}
