package dev.usbharu.hideout.core.domain.exception.media

import java.io.Serial

@Suppress("UnnecessaryAbstractClass")
abstract class MediaException : RuntimeException {
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
        private const val serialVersionUID: Long = 5988922562494187852L
    }
}
