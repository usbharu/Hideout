package dev.usbharu.hideout.mastodon.interfaces.api.status

import dev.usbharu.hideout.controller.mastodon.generated.StatusApi
import dev.usbharu.hideout.core.infrastructure.springframework.security.LoginUserContextHolder
import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import dev.usbharu.hideout.mastodon.service.status.StatusesApiService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class MastodonStatusesApiContoller(
    private val statusesApiService: StatusesApiService,
    private val loginUserContextHolder: LoginUserContextHolder
) : StatusApi {
    override suspend fun apiV1StatusesPost(
        devUsbharuHideoutDomainModelMastodonStatusesRequest: StatusesRequest
    ): ResponseEntity<Status> {
        val userid = loginUserContextHolder.getLoginUserId()

        return ResponseEntity(
            statusesApiService.postStatus(
                devUsbharuHideoutDomainModelMastodonStatusesRequest,
                userid
            ),
            HttpStatus.OK
        )
    }

    override suspend fun apiV1StatusesIdEmojiReactionsEmojiDelete(id: String, emoji: String): ResponseEntity<Status> {
        val uid =
            loginUserContextHolder.getLoginUserId()

        return ResponseEntity.ok(statusesApiService.removeEmojiReactions(id.toLong(), uid, emoji))
    }

    override suspend fun apiV1StatusesIdEmojiReactionsEmojiPut(id: String, emoji: String): ResponseEntity<Status> {
        val uid =
            loginUserContextHolder.getLoginUserId()

        return ResponseEntity.ok(statusesApiService.emojiReactions(id.toLong(), uid, emoji))
    }

    override suspend fun apiV1StatusesIdGet(id: String): ResponseEntity<Status> = super.apiV1StatusesIdGet(id)
}
