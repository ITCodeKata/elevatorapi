package com.project.api.common.httpserver

import java.io.{FileInputStream, InputStream}
import java.security.{KeyStore, SecureRandom}

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.settings.ServerSettings
import akka.http.scaladsl.{ConnectionContext, Http, HttpsConnectionContext}
import akka.stream.Materializer
import com.project.api.common.util.Tags.RootConfig
import com.softwaremill.tagging.@@
import com.typesafe.config.Config
import javax.net.ssl.{KeyManagerFactory, SSLContext}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

object HttpServer {

  def apply(httpConfig: HttpConfig, route: Route, config: Config @@ RootConfig)(
      implicit system: ActorSystem,
      mat: Materializer
  ): HttpServer =
    new HttpServer(ServerSettings(config), httpConfig, route)

}

class HttpServer private (setting: ServerSettings, httpConfig: HttpConfig, route: Route)(
    implicit system: ActorSystem,
    mat: Materializer
) {

  implicit val execContext: ExecutionContext = system.dispatcher
  private val promiseStop                    = Promise[Done]()
  private val promiseUnbound                 = Promise[Done]()

  protected def fServerStopping: Future[Done] = promiseStop.future

  protected def fServerUnbound: Future[Done] = promiseUnbound.future

  if (httpConfig.enableHttp) {
    Http()
      .bindAndHandle(
        handler = route,
        interface = httpConfig.interface,
        port = httpConfig.httpPort,
        settings = setting
      )
      .onComplete {
        case Success(binding) ⇒
          postBinding(binding, false)
        case Failure(cause) ⇒
          system.log.error(cause, s"Error starting `${system.name}` server! ${cause.getMessage}")
      }
  }

  protected val fServerBinding: Future[ServerBinding] = {
    val serverContext: HttpsConnectionContext = {
      val password              = httpConfig.certPassword.toCharArray
      val context               = SSLContext.getInstance("TLS")
      val ks                    = KeyStore.getInstance("PKCS12")
      val certFile: InputStream = new FileInputStream(httpConfig.certFile)

      ks.load(certFile, password)
      val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
      keyManagerFactory.init(ks, password)
      context.init(keyManagerFactory.getKeyManagers, null, new SecureRandom)

      ConnectionContext.https(sslContext = context)
    }
    Http().bindAndHandle(
      handler = route,
      interface = httpConfig.interface,
      port = httpConfig.httpsPort,
      connectionContext = serverContext,
      settings = setting
    )
  }

  fServerBinding.onComplete {
    case Success(binding) ⇒
      postBinding(binding)
      shutdownHook(binding)
    case Failure(cause) ⇒
      system.log.error(cause, s"Error starting `${system.name}` server! ${cause.getMessage}")
  }

  def stop(): Future[Done] = {
    promiseStop.trySuccess(Done)
    fServerUnbound
  }

  protected def postBinding(binding: ServerBinding, isHttps: Boolean = true): Unit =
    system.log.info(
      s"`${system.name}` server online at " +
        s"http${if (isHttps) "s" else ""}" +
        s"://${binding.localAddress.getHostName}:${binding.localAddress.getPort}"
    )

  protected def postStop(attempt: Try[Done]): Unit = {
    system.log.info(s"Stopping `${system.name}` server")
    attempt match {
      case Success(_) ⇒
        promiseUnbound.trySuccess(Done)
        system.log.info("system stopped!")
      case Failure(cause) ⇒
        system.log.info(s"Exception occurred during stopping system. error: $cause, errorMessage: ${cause.getMessage}")
    }
  }

  protected def shutdownHook(binding: ServerBinding): Unit = {
    sys.addShutdownHook {
      promiseStop.trySuccess(Done)
    }
    fServerStopping.onComplete { _ ⇒
      binding.unbind().onComplete(postStop)
    }
  }

}
