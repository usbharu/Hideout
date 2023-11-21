package dev.usbharu.hideout.application.infrastructure.exposed

import dev.usbharu.hideout.application.external.Transaction
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.springframework.stereotype.Service
import java.sql.Connection

@Service
class ExposedTransaction : Transaction {
    override suspend fun <T> transaction(block: suspend () -> T): T {
        return org.jetbrains.exposed.sql.transactions.transaction(transactionIsolation = Connection.TRANSACTION_SERIALIZABLE) {
            addLogger(StdOutSqlLogger)
            runBlocking {
                block()
            }
        }
    }

    override suspend fun <T> transaction(transactionLevel: Int, block: suspend () -> T): T {
        return newSuspendedTransaction(MDCContext(), transactionIsolation = transactionLevel) {
            addLogger(StdOutSqlLogger)
            block()
        }
    }
}
