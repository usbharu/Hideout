package dev.usbharu.hideout.controller.mastodon

import dev.usbharu.hideout.controller.mastodon.generated.StatusApi
import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import dev.usbharu.hideout.domain.mastodon.model.generated.StatusesRequest
import dev.usbharu.hideout.service.api.mastodon.StatusesApiService
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ModelAttribute

@Controller
class MastodonStatusesApiContoller(private val statusesApiService: StatusesApiService) : StatusApi {
    override fun apiV1StatusesPost(@ModelAttribute statusesRequest: StatusesRequest): ResponseEntity<Status> =
        runBlocking {
            val jwt = SecurityContextHolder.getContext().authentication.principal as Jwt

            ResponseEntity(
                statusesApiService.postStatus(statusesRequest, jwt.getClaim<String>("uid").toLong()),
                HttpStatus.OK
            )
    }
}
