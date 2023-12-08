package dev.usbharu.hideout.core.service.block

interface BlockService {
    suspend fun block(userId: Long, target: Long)
    suspend fun unblock(userId: Long, target: Long)
}
