package dev.usbharu.hideout.core.domain.model.media

import dev.usbharu.hideout.core.service.media.FileType
import dev.usbharu.hideout.core.service.media.MimeType

data class Media(
    val id: Long,
    val name: String,
    val url: String,
    val remoteUrl: String?,
    val thumbnailUrl: String?,
    val type: FileType,
    val mimeType: MimeType,
    val blurHash: String?
)
