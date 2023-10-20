package dev.usbharu.hideout.exception

import java.io.Serial
import javax.naming.AuthenticationException

class HttpSignatureVerifyException : AuthenticationException {
    constructor() : super()
    constructor(s: String?) : super(s)

    companion object {
        @Serial
        private const val serialVersionUID: Long = 1484943321770741944L
    }
}
