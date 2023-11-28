package dev.usbharu.hideout.core.service.media

data class ProcessedFile(
    val byteArray: ByteArray,
    val extension: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProcessedFile

        if (!byteArray.contentEquals(other.byteArray)) return false
        if (extension != other.extension) return false

        return true
    }

    override fun hashCode(): Int {
        var result = byteArray.contentHashCode()
        result = 31 * result + extension.hashCode()
        return result
    }
}
