package dev.usbharu.hideout.service.media

sealed class SavedMedia(val success: Boolean)

class SuccessSavedMedia(
    val name: String,
    val url: String,
    val thumbnailUrl: String,
    val blurhash: String
) :
    SavedMedia(true)


class FaildSavedMedia(
    val reason: String,
    val description: String,
    val trace: Throwable? = null
) : SavedMedia(false)