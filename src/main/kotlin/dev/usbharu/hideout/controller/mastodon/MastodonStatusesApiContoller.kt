package dev.usbharu.hideout.controller.mastodon

import dev.usbharu.hideout.controller.mastodon.generated.StatusApi
import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import dev.usbharu.hideout.domain.model.mastodon.StatusesRequest
import dev.usbharu.hideout.service.api.mastodon.StatusesApiService
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller

@Controller
class MastodonStatusesApiContoller(private val statusesApiService: StatusesApiService) : StatusApi {
    override fun apiV1StatusesPost(devUsbharuHideoutDomainModelMastodonStatusesRequest: StatusesRequest): ResponseEntity<Status> {
        return runBlocking {
            val jwt = SecurityContextHolder.getContext().authentication.principal as Jwt

            ResponseEntity(
                statusesApiService.postStatus(
                    devUsbharuHideoutDomainModelMastodonStatusesRequest,
                    jwt.getClaim<String>("uid").toLong()
                ),
                HttpStatus.OK
            )
        }
    }
}
