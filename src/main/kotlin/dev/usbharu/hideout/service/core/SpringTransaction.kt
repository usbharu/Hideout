package dev.usbharu.hideout.service.core

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Component
@Primary
class SpringTransaction : Transaction {
    override suspend fun <T> transaction(block: suspend () -> T): T = block()

    override suspend fun <T> transaction(transactionLevel: Int, block: suspend () -> T): T = block()
}
