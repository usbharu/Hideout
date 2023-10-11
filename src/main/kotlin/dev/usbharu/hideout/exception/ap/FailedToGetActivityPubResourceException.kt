package dev.usbharu.hideout.exception.ap

import dev.usbharu.hideout.exception.FailedToGetResourcesException

class FailedToGetActivityPubResourceException : FailedToGetResourcesException {
    constructor() : super()
    constructor(s: String?) : super(s)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}
