package dev.usbharu.hideout.activitypub.interfaces.api.webfinger

import dev.usbharu.hideout.activitypub.domain.model.webfinger.WebFinger
import dev.usbharu.hideout.activitypub.service.webfinger.WebFingerApiService
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.util.AcctUtil
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class WebFingerController(
    private val webFingerApiService: WebFingerApiService,
    private val applicationConfig: ApplicationConfig
) {
    @GetMapping("/.well-known/webfinger")
    fun webfinger(@RequestParam("resource") resource: String): ResponseEntity<WebFinger> = runBlocking {
        logger.info("WEBFINGER Lookup webfinger resource: {}", resource)
        val acct = try {
            AcctUtil.parse(resource.replace("acct:", ""))
        } catch (e: IllegalArgumentException) {
            logger.warn("FAILED Parse acct.", e)
            return@runBlocking ResponseEntity.badRequest().build()
        }
        val user =
            webFingerApiService.findByNameAndDomain(acct.username, acct.domain ?: applicationConfig.url.host)
        val webFinger = WebFinger(
            "acct:${user.name}@${user.domain}",
            listOf(
                WebFinger.Link(
                    "self",
                    "application/activity+json",
                    user.url
                )
            )
        )
        logger.info("SUCCESS Lookup webfinger resource: {} acct: {}", resource, acct)
        ResponseEntity(webFinger, HttpStatus.OK)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WebFingerController::class.java)
    }
}
