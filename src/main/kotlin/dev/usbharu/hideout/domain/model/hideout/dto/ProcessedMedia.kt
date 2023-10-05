package dev.usbharu.hideout.domain.model.hideout.dto

data class ProcessedMedia(
    val file: ProcessedFile,
    val thumbnail: ProcessedFile?
)
