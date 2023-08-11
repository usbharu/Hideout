package dev.usbharu.hideout.service.core

interface Transaction {
    suspend fun <T> transaction(block: suspend () -> T): T
}
