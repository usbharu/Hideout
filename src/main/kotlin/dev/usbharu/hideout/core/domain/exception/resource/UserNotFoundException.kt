package dev.usbharu.hideout.core.domain.exception.resource

import java.io.Serial

open class UserNotFoundException : NotFoundException {
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
        private const val serialVersionUID: Long = 3219433672235626200L

        fun withName(string: String, throwable: Throwable? = null): UserNotFoundException =
            UserNotFoundException("name: $string was not found.", throwable)

        fun withId(id: Long, throwable: Throwable? = null): UserNotFoundException =
            UserNotFoundException("id: $id was not found.", throwable)

        fun withUrl(url: String, throwable: Throwable? = null): UserNotFoundException =
            UserNotFoundException("url: $url was not found.", throwable)

        fun withNameAndDomain(name: String, domain: String, throwable: Throwable? = null): UserNotFoundException =
            UserNotFoundException("name: $name domain: $domain (@$name@$domain) was not found.", throwable)

        fun withKeyId(keyId: String, throwable: Throwable? = null) =
            UserNotFoundException("keyId: $keyId was not found.", throwable)
    }
}
