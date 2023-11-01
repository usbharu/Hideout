package dev.usbharu.hideout.core.domain.exception

import java.io.Serial

open class FailedToGetResourcesException : IllegalArgumentException {
    constructor() : super()
    constructor(s: String?) : super(s)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)

    companion object {
        @Serial
        private const val serialVersionUID: Long = -3117221954866309059L
    }
}
