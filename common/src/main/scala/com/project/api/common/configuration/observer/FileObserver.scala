package com.project.api.common.configuration.observer

import java.io.File

trait FileObserver {

  type CurrentValue

  protected def currentValue: CurrentValue

  val observedFile: File

  def isModified: Boolean

}
