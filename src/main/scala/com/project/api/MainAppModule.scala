package com.project.api

import akka.actor.{ActorSystem, Scheduler}
import akka.http.scaladsl.server.{Directive0, Route, RouteConcatenation}
import akka.stream.{ActorMaterializer, Materializer}
/*import com.project.api.client.http.HttpClientConfig*/
import com.project.api.common.module.{AppModule, ServiceModule}
import com.project.api.common.util.Tags.{AppConfig, RootConfig}
import com.softwaremill.macwire.wire
import com.softwaremill.tagging._
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory

import scala.concurrent.Future

// $COVERAGE-OFF$
class MainAppModule extends AppModule with RouteConcatenation {

  implicit override val system: ActorSystem        = ActorSystem("ceg-api-service")
  implicit override val materializer: Materializer = ActorMaterializer()
  implicit override val scheduler: Scheduler       = system.scheduler

  override val rootConfig: Config @@ RootConfig = ConfigFactory.load().taggedWith[RootConfig]
  override val appConfig: Config @@ AppConfig   = rootConfig.getConfig("ceg-api").taggedWith[AppConfig]

  import com.project.api.common.module.ExecutionContextModule.Implicits._

  protected val dbModule: MainDBModule = wire[MainDBModule]

  protected val serviceModule: ServiceModule = wire[MainServiceModule]

  override def route: Route = serviceModule.route
}
// $COVERAGE-ON$
