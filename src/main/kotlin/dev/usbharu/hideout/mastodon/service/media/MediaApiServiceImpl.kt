package dev.usbharu.hideout.mastodon.service.media

import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.service.media.FileType
import dev.usbharu.hideout.core.service.media.MediaService
import dev.usbharu.hideout.domain.mastodon.model.generated.MediaAttachment
import dev.usbharu.hideout.mastodon.interfaces.api.media.MediaRequest
import org.springframework.stereotype.Service

@Service
class MediaApiServiceImpl(private val mediaService: MediaService, private val transaction: Transaction) :
    MediaApiService {

    override suspend fun postMedia(mediaRequest: MediaRequest): MediaAttachment {
        return transaction.transaction {
            val uploadLocalMedia = mediaService.uploadLocalMedia(mediaRequest)
            val type = when (uploadLocalMedia.type) {
                FileType.Image -> MediaAttachment.Type.image
                FileType.Video -> MediaAttachment.Type.video
                FileType.Audio -> MediaAttachment.Type.audio
                FileType.Unknown -> MediaAttachment.Type.unknown
            }
            return@transaction MediaAttachment(
                id = uploadLocalMedia.id.toString(),
                type = type,
                url = uploadLocalMedia.url,
                previewUrl = uploadLocalMedia.thumbnailUrl,
                description = mediaRequest.description,
                blurhash = uploadLocalMedia.blurHash,
                textUrl = uploadLocalMedia.url
            )
        }
    }
}
