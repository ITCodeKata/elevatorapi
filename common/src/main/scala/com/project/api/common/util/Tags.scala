package com.project.api.common.util

object Tags {

  /** Configurations */
  trait RootConfig
  trait AppConfig

  /** Execution Contexts */
  trait IOExecContext
  trait FJExecContext
  trait DbIOBound

}
