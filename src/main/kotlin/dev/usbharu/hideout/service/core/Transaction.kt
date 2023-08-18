package dev.usbharu.hideout.service.core

import org.springframework.stereotype.Service

@Service
interface Transaction {
    suspend fun <T> transaction(block: suspend () -> T): T
    suspend fun <T> transaction(transactionLevel: Int, block: suspend () -> T): T
}
