package dev.usbharu.hideout.mastodon.interfaces.api.instance

import dev.usbharu.hideout.controller.mastodon.generated.InstanceApi
import dev.usbharu.hideout.domain.mastodon.model.generated.V1Instance
import dev.usbharu.hideout.mastodon.service.instance.InstanceApiService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class MastodonInstanceApiController(private val instanceApiService: InstanceApiService) : InstanceApi {
    override suspend fun apiV1InstanceGet(): ResponseEntity<V1Instance> =
        ResponseEntity(instanceApiService.v1Instance(), HttpStatus.OK)
}
