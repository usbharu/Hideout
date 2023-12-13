package dev.usbharu.hideout.core.domain.model.deletedActor

import java.time.Instant

data class DeletedActor(
    val id: Long,
    val name: String,
    val domain: String,
    val publicKey: String,
    val deletedAt: Instant
)
