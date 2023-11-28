package dev.usbharu.hideout.activitypub.domain.exception

import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import java.io.Serial

class FailedToGetActivityPubResourceException : FailedToGetResourcesException {
    constructor() : super()
    constructor(s: String?) : super(s)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)

    companion object {
        @Serial
        private const val serialVersionUID: Long = 6420233106776818052L
    }
}
