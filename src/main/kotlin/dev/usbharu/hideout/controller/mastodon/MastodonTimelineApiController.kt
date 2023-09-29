package dev.usbharu.hideout.controller.mastodon

import dev.usbharu.hideout.controller.mastodon.generated.TimelineApi
import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class MastodonTimelineApiController : TimelineApi {
    override fun apiV1TimelinesHomeGet(
        maxId: String?,
        sinceId: String?,
        minId: String?,
        limit: Int?
    ): ResponseEntity<List<Status>> {

    }

    override fun apiV1TimelinesPublicGet(
        local: Boolean?,
        remote: Boolean?,
        onlyMedia: Boolean?,
        maxId: String?,
        sinceId: String?,
        minId: String?,
        limit: Int?
    ): ResponseEntity<List<Status>> {

    }
}
