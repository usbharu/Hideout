package dev.usbharu.hideout.core.application.model

import dev.usbharu.hideout.core.domain.model.media.Media
import java.net.URI

data class MediaDetail(
    val mediaId: Long,
    val type: String,
    val url: URI,
    val thumbnailUrl: URI?,
    val sensitive: Boolean,
    val description: String,
    val blurhash: String,
    val actorId: Long
) {
    companion object {
        fun of(media: Media): MediaDetail {
            return MediaDetail(
                mediaId = media.id.id,
                type = media.type.name,
                url = media.url,
                thumbnailUrl = media.thumbnailUrl,
                sensitive = false,
                description = media.description?.description.orEmpty(),
                blurhash = media.blurHash?.hash.orEmpty(),
                actorId = media.actorId.id
            )
        }
    }
}
