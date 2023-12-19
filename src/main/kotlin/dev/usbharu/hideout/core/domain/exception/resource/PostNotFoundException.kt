package dev.usbharu.hideout.core.domain.exception.resource

import java.io.Serial

class PostNotFoundException : NotFoundException {
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
        private const val serialVersionUID: Long = 1315818410686905717L

        fun withApId(apId: String): PostNotFoundException = PostNotFoundException("apId: $apId was not found.")
    }
}
