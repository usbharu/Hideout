package dev.usbharu.hideout.core.service.media

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
