package dev.usbharu.hideout.activitypub.interfaces.wellknown

import dev.usbharu.hideout.core.config.ApplicationConfig
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/.well-known")
class NodeinfoController(private val applicationConfig: ApplicationConfig) {
    @GetMapping("/nodeinfo", produces = ["application/json"])
    suspend fun nodeinfo(): XRD = XRD(
        listOf(
            Link(
                "http://nodeinfo.diaspora.software/ns/schema/2.1",
                href = applicationConfig.url.resolve("/nodeinfo/2.1").toString()
            ), Link(
                "http://nodeinfo.diaspora.software/ns/schema/2.0",
                href = applicationConfig.url.resolve("/nodeinfo/2.0").toString()
            )
        )
    )
}