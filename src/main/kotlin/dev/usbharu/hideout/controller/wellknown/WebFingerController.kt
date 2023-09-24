package dev.usbharu.hideout.controller.wellknown

import dev.usbharu.hideout.config.ApplicationConfig
import dev.usbharu.hideout.domain.model.wellknown.WebFinger
import dev.usbharu.hideout.service.api.WebFingerApiService
import dev.usbharu.hideout.util.AcctUtil
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.net.URL

@Controller
@RequestMapping("/.well-known")
class WebFingerController(
    private val webFingerApiService: WebFingerApiService,
    private val applicationConfig: ApplicationConfig
) {
    @GetMapping("/webfinger")
    suspend fun webfinger(@RequestParam resource: String): ResponseEntity<WebFinger> {
        val acct = AcctUtil.parse(resource)
        val user =
            webFingerApiService.findByNameAndDomain(acct.username, acct.domain ?: URL(applicationConfig.url).host)
        val webFinger = WebFinger(
            "acct:${user.name}@${user.domain}",
            listOf(
                WebFinger.Link(
                    "self",
                    "application/activity+json",
                    applicationConfig.url + "/users/" + user.id
                )
            )
        )
        return ResponseEntity(webFinger, HttpStatus.OK)
    }
}
