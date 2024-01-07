package dev.usbharu.hideout.mastodon.interfaces.api.status

import dev.usbharu.hideout.controller.mastodon.generated.StatusApi
import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import dev.usbharu.hideout.mastodon.service.status.StatusesApiService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller

@Controller
class MastodonStatusesApiContoller(private val statusesApiService: StatusesApiService) : StatusApi {
    override suspend fun apiV1StatusesPost(
        devUsbharuHideoutDomainModelMastodonStatusesRequest: StatusesRequest
    ): ResponseEntity<Status> {
        val jwt = SecurityContextHolder.getContext().authentication.principal as Jwt

        return ResponseEntity(
            statusesApiService.postStatus(
                devUsbharuHideoutDomainModelMastodonStatusesRequest,
                jwt.getClaim<String>("uid").toLong()
            ),
            HttpStatus.OK
        )
    }

    override suspend fun apiV1StatusesIdEmojiReactionsEmojiDelete(id: String, emoji: String): ResponseEntity<Status> {
        return super.apiV1StatusesIdEmojiReactionsEmojiDelete(id, emoji)
    }

    override suspend fun apiV1StatusesIdEmojiReactionsEmojiPut(id: String, emoji: String): ResponseEntity<Status> {
        return super.apiV1StatusesIdEmojiReactionsEmojiPut(id, emoji)
    }

    override suspend fun apiV1StatusesIdGet(id: String): ResponseEntity<Status> {
        return super.apiV1StatusesIdGet(id)
    }
}
