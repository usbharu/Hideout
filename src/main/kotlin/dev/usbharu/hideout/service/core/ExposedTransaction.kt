package dev.usbharu.hideout.service.core

import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.annotation.Single
import org.springframework.stereotype.Service

@Single
@Service
class ExposedTransaction : Transaction {
    override suspend fun <T> transaction(block: suspend () -> T): T {
        return newSuspendedTransaction(transactionIsolation = java.sql.Connection.TRANSACTION_SERIALIZABLE) {
            block()
        }
    }

    override suspend fun <T> transaction(transactionLevel: Int, block: suspend () -> T): T {
        return newSuspendedTransaction(transactionIsolation = transactionLevel) {
            block()
        }
    }
}
