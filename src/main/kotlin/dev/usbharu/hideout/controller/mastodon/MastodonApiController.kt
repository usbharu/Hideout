package dev.usbharu.hideout.controller.mastodon

import dev.usbharu.hideout.controller.mastodon.generated.DefaultApi
import dev.usbharu.hideout.domain.mastodon.model.generated.V1Instance
import dev.usbharu.hideout.service.api.mastodon.InstanceApiService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class MastodonApiController(private val instanceApiService: InstanceApiService) : DefaultApi {
    override suspend fun apiV1InstanceGet(): ResponseEntity<V1Instance> {
        return ResponseEntity(instanceApiService.v1Instance(), HttpStatus.OK)
    }
}
