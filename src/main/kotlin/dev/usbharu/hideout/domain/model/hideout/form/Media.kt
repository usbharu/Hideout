package dev.usbharu.hideout.domain.model.hideout.form

import org.springframework.web.multipart.MultipartFile

data class Media(
    val file: MultipartFile,
    val thumbnail: MultipartFile?,
    val description: String?,
    val focus: String?
)
