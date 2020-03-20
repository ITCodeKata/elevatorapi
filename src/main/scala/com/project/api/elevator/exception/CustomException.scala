package com.project.api.elevator.exception

import com.project.api.common.exception.InternalBaseException

final case class ElevatorException(
    override val message: String = "ElevatorException",
    override val cause: Option[Throwable] = None
) extends InternalBaseException(message, cause)
