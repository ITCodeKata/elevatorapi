package com.project.api

import com.project.api.common.util.Tags.{FJExecContext, IOExecContext, RootConfig}
import com.softwaremill.macwire.{Module, wire}
import com.softwaremill.tagging._
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext

@Module
class MainDBModule(rootConfig: Config @@ RootConfig)(
    implicit
    ioBoundCtx: ExecutionContext @@ IOExecContext,
    fjExecCtx: ExecutionContext @@ FJExecContext
) {

}
