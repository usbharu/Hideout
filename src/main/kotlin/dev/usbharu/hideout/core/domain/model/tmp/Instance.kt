package dev.usbharu.hideout.core.domain.model.tmp

import java.time.Instant

data class Instance(
    val id: Long,
    val name: String,
    val description: String,
    val url: String,
    val iconUrl: String,
    val sharedInbox: String?,
    val sowtware: String?,
    val version: String?,
    val isBlocked: Boolean,
    val isMuted: Boolean,
    val moderationNote: String,
    val createdAt: Instant,
    val updatedAt: Instant
)
