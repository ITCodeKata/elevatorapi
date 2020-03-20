package com.project.api.common.configuration

import java.io.File
import java.util.concurrent.atomic.AtomicReference

import com.project.api.common.configuration.observer.{FileLastModifiedObserver, FileObserver}
import com.typesafe.config.{Config, ConfigFactory}
import net.ceedubs.ficus.FicusConfig

final class DynamicConfig(fileObserver: FileObserver) extends FicusConfig {

  private def readConfigFromFile = ConfigFactory.parseFile(fileObserver.observedFile)

  private val cachedConfig: AtomicReference[Config] = new AtomicReference(readConfigFromFile)

  override def config: Config = {

    val current = cachedConfig.get

    if (fileObserver.isModified) {
      val newConfig = readConfigFromFile
      if (cachedConfig.compareAndSet(current, newConfig)) {
        newConfig
      } else {
        cachedConfig.get
      }
    } else {
      current
    }

  }

}

object DynamicConfig {

  def apply(envProjectPath: String, resourceBasename: String): DynamicConfig = {

    val pathName = Option(System.getProperty("app_as_service", null))
      .map(_ ⇒ s"$envProjectPath/$resourceBasename")             // For PROD, QA, DEV (Package deployment via CSpider)
      .getOrElse(s"target/scala-2.12/classes/$resourceBasename") // For local

    val fileObserver = new FileLastModifiedObserver(new File(pathName))

    new DynamicConfig(fileObserver)
  }
}
