package com.project.api.common.exception

class InternalBaseException(val message: String = "", val cause: Option[Throwable] = None)
    extends Exception(message, cause.orNull)
