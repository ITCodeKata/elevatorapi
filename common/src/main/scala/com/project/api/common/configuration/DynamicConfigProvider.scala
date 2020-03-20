package com.project.api.common.configuration

import java.util.concurrent.atomic.AtomicReference
import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ValueReader
import scala.util.Try

trait DynamicConfigProvider[A] {
  final def get: Try[A] = Try(unsafeGet)
  def unsafeGet: A

  final def getWithState: Try[Either[A, A]] = Try(unsafeGetWithState)
  def unsafeGetWithState: Either[A, A]

}

object DynamicConfigProvider {

  def apply[A: ValueReader](dynamicConfig: DynamicConfig, path: String): DynamicConfigProvider[A] =
    new DefaultImpl[A](dynamicConfig, path)

  private class DefaultImpl[A: ValueReader](dynamicConfig: DynamicConfig, path: String)
      extends DynamicConfigProvider[A] {

    private val cached: AtomicReference[(Config, A)] = {
      val config = dynamicConfig.config
      new AtomicReference((config, config.as[A](path)))
    }

    private def _unsafeGet(pair: (Config, A), currentConfig: Config): A = {

      val (cachedConfig, cachedA) = pair

      if (currentConfig.eq(cachedConfig)) {
        cachedA
      } else {

        val newA      = currentConfig.as[A](path)
        val newCached = (currentConfig, newA)

        if (cached.compareAndSet(pair, newCached)) {
          newA
        } else {
          cached.get()._2
        }
      }

    }

    override def unsafeGetWithState: Either[A, A] = {

      val pair   = cached.get
      val config = dynamicConfig.config
      val a      = _unsafeGet(pair, config)

      if (!config.eq(pair._1)) Left(a) else Right(a)

    }

    override def unsafeGet: A =
      _unsafeGet(cached.get, dynamicConfig.config)

  }

}
