package dev.usbharu.hideout.activitypub.interfaces.wellknown

import dev.usbharu.hideout.activitypub.application.webfinger.WebFingerApplicationService
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/.well-known")
class WebFingerController(
    private val webFingerApplicationService: WebFingerApplicationService,
) {
    @GetMapping("/webfinger", produces = ["application/json"])
    suspend fun webfinger(@RequestParam(name = "resource", required = true) resource: String): XRD =
        webFingerApplicationService.execute(resource, Anonymous)
}
