package dev.usbharu.hideout.core.domain.exception.media

import java.io.Serial

class RemoteMediaFileSizeException : MediaFileSizeException {
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
        private const val serialVersionUID: Long = 9188247721397839435L
    }
}
