package com.project.api.common.configuration.observer

import java.io.File
import java.util.concurrent.atomic.AtomicLong

/**
  * FileLastModifiedObserver observes file changes by comparing a last modified value
  * whether or not a new number, read from the configuration file, is greater than the current value
  *
  * @param observedFile the file
  */
final class FileLastModifiedObserver(override val observedFile: File) extends FileObserver {

  type CurrentValue = AtomicLong

  override protected val currentValue: CurrentValue = new AtomicLong(observedFile.lastModified())

  override def isModified: Boolean = {
    val curValue = currentValue.get()
    val newValue = observedFile.lastModified()

    curValue < newValue && { currentValue.set(newValue); true }
  }

}
