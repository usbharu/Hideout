package dev.usbharu.hideout.exception

import java.io.Serial

class InvalidUsernameOrPasswordException : IllegalArgumentException {
    constructor() : super()
    constructor(s: String?) : super(s)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)

    companion object {
        @Serial
        private const val serialVersionUID: Long = -3638699928983322003L
    }
}
