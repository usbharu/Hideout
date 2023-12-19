package dev.usbharu.hideout.core.domain.exception

import dev.usbharu.hideout.core.domain.exception.resource.DuplicateException
import dev.usbharu.hideout.core.domain.exception.resource.ResourceAccessException
import org.springframework.dao.DataAccessException
import org.springframework.dao.DuplicateKeyException

class SpringDataAccessExceptionSQLExceptionTranslator : SQLExceptionTranslator {
    override fun translate(message: String, sql: String?, exception: Exception): ResourceAccessException {
        if (exception !is DataAccessException) {
            throw IllegalArgumentException("exception must be DataAccessException.")
        }

        return when (exception) {
            is DuplicateKeyException -> DuplicateException(message, exception.rootCause)
            else -> ResourceAccessException(message, exception.rootCause)
        }
    }
}
