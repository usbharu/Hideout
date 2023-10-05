package dev.usbharu.hideout.domain.model.hideout.entity

import dev.usbharu.hideout.domain.model.hideout.dto.FileType

data class Media(
    val id: Long,
    val name: String,
    val url: String,
    val remoteUrl: String?,
    val thumbnailUrl: String?,
    val type: FileType,
    val blurHash: String?
)
