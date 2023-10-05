package dev.usbharu.hideout.service.api.mastodon

import dev.usbharu.hideout.domain.mastodon.model.generated.MediaAttachment
import dev.usbharu.hideout.domain.model.hideout.dto.FileType
import dev.usbharu.hideout.domain.model.hideout.form.Media
import dev.usbharu.hideout.service.core.Transaction
import dev.usbharu.hideout.service.media.MediaService
import org.springframework.stereotype.Service

@Service
class MediaApiServiceImpl(private val mediaService: MediaService, private val transaction: Transaction) :
    MediaApiService {

    override suspend fun postMedia(media: Media): MediaAttachment {
        return transaction.transaction {

            val uploadLocalMedia = mediaService.uploadLocalMedia(media)
            val type = when (uploadLocalMedia.type) {
                FileType.Image -> MediaAttachment.Type.image
                FileType.Video -> MediaAttachment.Type.video
                FileType.Audio -> MediaAttachment.Type.audio
                FileType.Unknown -> MediaAttachment.Type.unknown
            }
            return@transaction MediaAttachment(
                uploadLocalMedia.id.toString(),
                type,
                uploadLocalMedia.url,
                uploadLocalMedia.thumbnailUrl,
                null,
                media.description,
                uploadLocalMedia.blurHash,
                uploadLocalMedia.url
            )
        }
    }
}
