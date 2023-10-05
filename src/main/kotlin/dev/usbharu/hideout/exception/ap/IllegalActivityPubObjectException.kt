package dev.usbharu.hideout.exception.ap

import java.io.Serial

class IllegalActivityPubObjectException : IllegalArgumentException {
    constructor() : super()
    constructor(s: String?) : super(s)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)

    companion object {
        @Serial
        private const val serialVersionUID: Long = 7216998115771415263L
    }
}
