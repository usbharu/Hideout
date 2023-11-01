package dev.usbharu.hideout.core.domain.model.media

import dev.usbharu.hideout.core.service.media.FileType

data class Media(
    val id: Long,
    val name: String,
    val url: String,
    val remoteUrl: String?,
    val thumbnailUrl: String?,
    val type: FileType,
    val blurHash: String?
)
