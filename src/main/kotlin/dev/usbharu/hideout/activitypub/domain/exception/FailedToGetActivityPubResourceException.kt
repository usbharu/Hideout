package dev.usbharu.hideout.activitypub.domain.exception

import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException

class FailedToGetActivityPubResourceException : FailedToGetResourcesException {
    constructor() : super()
    constructor(s: String?) : super(s)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}
