package dev.usbharu.hideout.core.application.post

import dev.usbharu.hideout.core.domain.model.media.Media
import java.net.URI

data class MediaDetail(
    val mediaId: Long,
    val type: String,
    val url: URI,
    val thumbnailUrl: URI?,
    val sensitive: Boolean,
    val description: String,
    val blurhash: String
) {
    companion object {
        fun of(media: Media): MediaDetail {
            return MediaDetail(
                media.id.id,
                media.type.name,
                media.url,
                media.thumbnailUrl,
                false,
                media.description?.description.orEmpty(),
                media.blurHash?.hash.orEmpty()
            )
        }
    }
}
