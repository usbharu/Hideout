package dev.usbharu.hideout.activitypub.interfaces.wellknown

import dev.usbharu.hideout.activitypub.application.nodeinfo.Nodeinfo2_0
import dev.usbharu.hideout.activitypub.application.nodeinfo.NodeinfoApplicationService
import dev.usbharu.hideout.core.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class NodeinfoController(
    private val applicationConfig: ApplicationConfig,
    private val nodeinfoApplicationService: NodeinfoApplicationService,
) {
    @GetMapping("/.well-known/nodeinfo", produces = ["application/json"])
    fun nodeinfo(): XRD = XRD(
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

    @GetMapping("/nodeinfo/2.0", produces = ["application/json"])
    suspend fun nodeinfo2_0(): Nodeinfo2_0 {
        return nodeinfoApplicationService.execute(Unit, Anonymous)
    }

    @GetMapping("/nodeinfo/2.1", produces = ["application/json"])
    suspend fun nodeinfo2_1(): Nodeinfo2_0 {
        return nodeinfoApplicationService.execute(Unit, Anonymous)
    }
}