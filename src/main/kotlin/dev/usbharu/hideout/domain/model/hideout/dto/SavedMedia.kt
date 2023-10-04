package dev.usbharu.hideout.domain.model.hideout.dto

sealed class SavedMedia(val success: Boolean)

class SuccessSavedMedia(
    val name: String,
    val url: String,
    val thumbnailUrl: String,
) :
    SavedMedia(true)


class FaildSavedMedia(
    val reason: String,
    val description: String,
    val trace: Throwable? = null
) : SavedMedia(false)
