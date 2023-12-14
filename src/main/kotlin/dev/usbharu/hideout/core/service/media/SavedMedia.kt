package dev.usbharu.hideout.core.service.media

sealed class SavedMedia(val success: Boolean) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SavedMedia

        return success == other.success
    }

    override fun hashCode(): Int {
        return success.hashCode()
    }
}

class SuccessSavedMedia(
    val name: String,
    val url: String,
    val thumbnailUrl: String,
) :
    SavedMedia(true) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as SuccessSavedMedia

        if (name != other.name) return false
        if (url != other.url) return false
        if (thumbnailUrl != other.thumbnailUrl) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + thumbnailUrl.hashCode()
        return result
    }
}

class FaildSavedMedia(
    val reason: String,
    val description: String,
    val trace: Throwable? = null
) : SavedMedia(false) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as FaildSavedMedia

        if (reason != other.reason) return false
        if (description != other.description) return false
        if (trace != other.trace) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + reason.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + (trace?.hashCode() ?: 0)
        return result
    }
}
