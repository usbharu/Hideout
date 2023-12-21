package dev.usbharu.hideout.core.domain.exception.resource.local

import dev.usbharu.hideout.core.domain.exception.resource.UserNotFoundException
import java.io.Serial

class LocalUserNotFoundException : UserNotFoundException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean) : super(
        message,
        cause,
        enableSuppression,
        writableStackTrace
    )

    companion object {
        @Serial
        private const val serialVersionUID: Long = -4742548128672528145L

        fun withName(string: String, throwable: Throwable? = null): LocalUserNotFoundException =
            LocalUserNotFoundException("name: $string was not found.", throwable)

        fun withId(id: Long, throwable: Throwable? = null): LocalUserNotFoundException =
            LocalUserNotFoundException("id: $id was not found.", throwable)

        fun withUrl(url: String, throwable: Throwable? = null): LocalUserNotFoundException =
            LocalUserNotFoundException("url: $url was not found.", throwable)
    }
}
