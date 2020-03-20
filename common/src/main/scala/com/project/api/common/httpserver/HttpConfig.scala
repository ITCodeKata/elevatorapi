package com.project.api.common.httpserver

import com.typesafe.config.Config

import scala.util.Try

case class HttpConfig(
    interface: String,
    httpPort: Int,
    httpsPort: Int,
    certFile: String,
    certPassword: String,
    enableHttp: Boolean
)

object HttpConfig {

  def readFrom(appConfig: Config): Try[HttpConfig] = Try { unsafeReadFrom(appConfig) }

  def unsafeReadFrom(appConfig: Config): HttpConfig = {

    val config = appConfig.getConfig("http-service")

    HttpConfig(
      interface = config.getString("interface"),
      httpPort = config.getInt("http-port"),
      httpsPort = config.getInt("https-port"),
      certFile = config.getString("cert-file"),
      certPassword = config.getString("cert-password"),
      enableHttp = config.getBoolean("enable-http")
    )

  }

}
