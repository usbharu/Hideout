package dev.usbharu.hideout.mastodon.interfaces.api.media

import org.springframework.web.multipart.MultipartFile

data class MediaRequest(
    val file: MultipartFile,
    val thumbnail: MultipartFile?,
    val description: String?,
    val focus: String?
)
