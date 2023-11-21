package dev.usbharu.hideout.application.infrastructure.exposed

import dev.usbharu.hideout.application.external.Transaction
import kotlinx.coroutines.slf4j.MDCContext
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.springframework.stereotype.Service

@Service
class ExposedTransaction : Transaction {
    override suspend fun <T> transaction(block: suspend () -> T): T {
        return newSuspendedTransaction(MDCContext()) {
            block()
        }
    }

    override suspend fun <T> transaction(transactionLevel: Int, block: suspend () -> T): T {
        return newSuspendedTransaction(MDCContext(), transactionIsolation = transactionLevel) {
            block()
        }
    }
}
