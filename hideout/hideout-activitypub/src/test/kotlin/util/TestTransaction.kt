package util

import dev.usbharu.hideout.core.application.shared.Transaction

object TestTransaction : Transaction {
    override suspend fun <T> transaction(block: suspend () -> T): T = block()
    override suspend fun <T> transaction(transactionLevel: Int, block: suspend () -> T): T = block()
}
