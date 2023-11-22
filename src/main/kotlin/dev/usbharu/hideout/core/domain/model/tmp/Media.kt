package dev.usbharu.hideout.core.domain.model.tmp

import java.time.Instant

data class Media(
    val id: Long,
    val name: String,
    val url: String,
    val remoteUrl: String?,
    val thumbnailUrl: String?,
    val mimeType: String,
    val blurHash: String?,
    val description: String?,
    val createdAt: Instant
)
