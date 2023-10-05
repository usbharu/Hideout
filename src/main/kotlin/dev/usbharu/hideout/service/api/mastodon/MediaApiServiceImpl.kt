package dev.usbharu.hideout.service.api.mastodon

import dev.usbharu.hideout.domain.mastodon.model.generated.MediaAttachment
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
            return@transaction MediaAttachment(

            )
        }
    }
}
