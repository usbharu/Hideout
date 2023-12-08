package dev.usbharu.hideout.core.domain.model.block

interface BlockRepository {
    suspend fun save(block: Block): Block
    suspend fun delete(block: Block)
    suspend fun findByUserIdAndTarget(userId: Long, target: Long): Block
}
