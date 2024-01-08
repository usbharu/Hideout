package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.core.domain.exception.SpringDataAccessExceptionSQLExceptionTranslator
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator
import java.sql.SQLException

@Suppress("VarCouldBeVal")
abstract class AbstractRepository {
    protected abstract val logger: Logger
    private val sqlErrorCodeSQLExceptionTranslator = SQLErrorCodeSQLExceptionTranslator()
    private val springDataAccessExceptionSQLExceptionTranslator = SpringDataAccessExceptionSQLExceptionTranslator()

    @Value("\${hideout.debug.trace-query-exception:false}")
    private var traceQueryException: Boolean = false

    @Value("\${hideout.debug.trace-query-call:false}")
    private var traceQueryCall: Boolean = false

    protected suspend fun <T> query(block: () -> T): T = try {
        if (traceQueryCall) {
            @Suppress("ThrowingExceptionsWithoutMessageOrCause")
            logger.trace(
                """
***** QUERY CALL STACK TRACE *****

${Throwable().stackTrace.joinToString("\n")}

***** QUERY CALL STACK TRACE *****
"""
            )
        }

        block.invoke()
    } catch (e: SQLException) {
        if (traceQueryException) {
            logger.trace("FAILED EXECUTE SQL", e)
        }
        TransactionManager.currentOrNull()?.rollback()
        if (e.cause !is SQLException) {
            throw e
        }

        val dataAccessException =
            sqlErrorCodeSQLExceptionTranslator.translate("Failed to persist entity", null, e.cause as SQLException)
                ?: throw e

        if (traceQueryException) {
            logger.trace("EXCEPTION TRANSLATED TO", dataAccessException)
        }

        val translate = springDataAccessExceptionSQLExceptionTranslator.translate(
            "Failed to persist entity",
            null,
            dataAccessException
        )

        if (traceQueryException) {
            logger.trace("EXCEPTION TRANSLATED TO", translate)
        }
        throw translate
    }
}
