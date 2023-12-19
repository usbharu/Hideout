package dev.usbharu.hideout.core.domain.exception

import dev.usbharu.hideout.core.domain.exception.resource.ResourceAccessException

interface SQLExceptionTranslator {
    fun translate(message: String, sql: String? = null, exception: Exception): ResourceAccessException
}
