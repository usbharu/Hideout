package dev.usbharu.hideout.mastodon.interfaces.api.media

import dev.usbharu.hideout.controller.mastodon.generated.MediaApi
import dev.usbharu.hideout.domain.mastodon.model.generated.MediaAttachment
import dev.usbharu.hideout.mastodon.service.media.MediaApiService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.multipart.MultipartFile

@Controller
class MastodonMediaApiController(private val mediaApiService: MediaApiService) : MediaApi {
    override suspend fun apiV1MediaPost(
        file: MultipartFile,
        thumbnail: MultipartFile?,
        description: String?,
        focus: String?
    ): ResponseEntity<MediaAttachment> {
        return ResponseEntity.ok(
            mediaApiService.postMedia(
                MediaRequest(
                    file,
                    thumbnail,
                    description,
                    focus
                )
            )
        )
    }
}
