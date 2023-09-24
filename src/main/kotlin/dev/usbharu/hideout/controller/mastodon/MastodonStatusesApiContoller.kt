package dev.usbharu.hideout.controller.mastodon

import dev.usbharu.hideout.controller.mastodon.generated.StatusApi
import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import dev.usbharu.hideout.domain.mastodon.model.generated.StatusesRequest
import dev.usbharu.hideout.domain.model.UserDetailsImpl
import dev.usbharu.hideout.service.api.mastodon.StatusesApiService
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller

@Controller
class MastodonStatusesApiContoller(private val statusesApiService: StatusesApiService) : StatusApi {
    override fun apiV1StatusesPost(statusesRequest: StatusesRequest): ResponseEntity<Status> = runBlocking {
        val principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal()
        require(principal is UserDetailsImpl)
        ResponseEntity(statusesApiService.postStatus(statusesRequest, principal), HttpStatus.OK)
    }
}
