package com.project.api.common.module

import java.util.concurrent.{Executors, ThreadFactory}

import cats.effect.{ContextShift, IO, Timer}
import com.project.api.common.util.Tags.{DbIOBound, FJExecContext, IOExecContext}
import com.softwaremill.tagging._

import scala.concurrent.ExecutionContext

object ExecutionContextModule {

  private def threadFactory(namePrefix: String): ThreadFactory = (r: Runnable) ⇒ {
    val thread = new Thread(r)
    thread.setDaemon(false)
    thread.setUncaughtExceptionHandler((_: Thread, cause: Throwable) ⇒ ExecutionContext.defaultReporter(cause))
    thread.setName(namePrefix + thread.getId)
    thread
  }

  object Implicits {

    implicit lazy val fjCtx: ExecutionContext @@ FJExecContext =
      ExecutionContext.Implicits.global.taggedWith[FJExecContext]

    implicit lazy val ctxShiftFj: ContextShift[IO] @@ FJExecContext = IO.contextShift(fjCtx).taggedWith[FJExecContext]

    implicit lazy val timerIO: Timer[IO] = IO.timer(fjCtx)

    implicit lazy val ioCtx: ExecutionContext @@ IOExecContext =
      ExecutionContext
        .fromExecutor(
          Executors.newFixedThreadPool(8, threadFactory("blocking-io"))
        )
        .taggedWith[IOExecContext]

    implicit lazy val ctxShiftIO: ContextShift[IO] @@ IOExecContext = IO.contextShift(ioCtx).taggedWith[IOExecContext]

    implicit lazy val dbCtx: ExecutionContext @@ DbIOBound =
      ExecutionContext
        .fromExecutor(
          Executors.newCachedThreadPool(threadFactory("db-io"))
        )
        .taggedWith[DbIOBound]

    implicit lazy val ctxShiftDb: ContextShift[IO] @@ DbIOBound = IO.contextShift(ioCtx).taggedWith[DbIOBound]

  }

}
