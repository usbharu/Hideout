package dev.usbharu.hideout.application.infrastructure.exposed

import dev.usbharu.hideout.application.external.Transaction
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import org.jetbrains.exposed.sql.Slf4jSqlDebugLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import java.sql.Connection

@Service
class ExposedTransaction : Transaction {
    override suspend fun <T> transaction(block: suspend () -> T): T {
        return transaction(transactionIsolation = Connection.TRANSACTION_READ_COMMITTED) {
            debug = true
            warnLongQueriesDuration = 1000
            addLogger(Slf4jSqlDebugLogger)
            runBlocking(MDCContext()) {
                block()
            }
        }
    }

    override suspend fun <T> transaction(transactionLevel: Int, block: suspend () -> T): T {
        return newSuspendedTransaction(MDCContext(), transactionIsolation = transactionLevel) {
            addLogger(Slf4jSqlDebugLogger)
            block()
        }
    }
}
