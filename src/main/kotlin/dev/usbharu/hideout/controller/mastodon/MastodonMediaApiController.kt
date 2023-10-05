package dev.usbharu.hideout.controller.mastodon

import dev.usbharu.hideout.controller.mastodon.generated.MediaApi
import dev.usbharu.hideout.domain.mastodon.model.generated.MediaAttachment
import dev.usbharu.hideout.domain.model.hideout.form.Media
import dev.usbharu.hideout.service.api.mastodon.MediaApiService
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.multipart.MultipartFile

@Controller
class MastodonMediaApiController(private val mediaApiService: MediaApiService) : MediaApi {
    override fun apiV1MediaPost(
        file: MultipartFile,
        thumbnail: MultipartFile?,
        description: String?,
        focus: String?
    ): ResponseEntity<MediaAttachment> = runBlocking {
        ResponseEntity.ok(
            mediaApiService.postMedia(
                Media(
                    file,
                    thumbnail,
                    description,
                    focus
                )
            )
        )
    }
}
