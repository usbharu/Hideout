package dev.usbharu.hideout.controller.mastodon

import dev.usbharu.hideout.controller.mastodon.generated.DefaultApi
import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import dev.usbharu.hideout.domain.mastodon.model.generated.StatusesRequest
import dev.usbharu.hideout.domain.mastodon.model.generated.V1Instance
import dev.usbharu.hideout.domain.model.UserDetailsImpl
import dev.usbharu.hideout.service.api.mastodon.InstanceApiService
import dev.usbharu.hideout.service.api.mastodon.StatusesApiService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller


@Controller
class MastodonApiController(
    private val instanceApiService: InstanceApiService,
    private val statusesApiService: StatusesApiService
) : DefaultApi {
    override suspend fun apiV1InstanceGet(): ResponseEntity<V1Instance> {
        return ResponseEntity(instanceApiService.v1Instance(), HttpStatus.OK)
    }

    override suspend fun apiV1StatusesPost(statusesRequest: StatusesRequest): ResponseEntity<Status> {
        val principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal()
        require(principal is UserDetailsImpl)
        return ResponseEntity(statusesApiService.postStatus(statusesRequest, principal), HttpStatus.OK)
    }
}
