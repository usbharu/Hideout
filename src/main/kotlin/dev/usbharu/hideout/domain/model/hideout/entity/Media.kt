package dev.usbharu.hideout.domain.model.hideout.entity

import dev.usbharu.hideout.service.media.FileTypeDeterminationService

data class Media(
    val id: Long,
    val name: String,
    val url: String,
    val remoteUrl: String?,
    val thumbnailUrl: String?,
    val type: FileTypeDeterminationService.FileType,
    val blurHash: String?
)
