package dev.usbharu.hideout.controller.mastodon

import dev.usbharu.hideout.controller.mastodon.generated.MediaApi
import dev.usbharu.hideout.domain.mastodon.model.generated.MediaAttachment
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.multipart.MultipartFile

@Controller
class MastodonMediaApiController : MediaApi {
    override fun apiV1MediaPost(
        file: MultipartFile,
        thumbnail: MultipartFile?,
        description: String?,
        focus: String?
    ): ResponseEntity<MediaAttachment> {

    }
}
