package dev.usbharu.hideout.controller

import dev.usbharu.hideout.service.ap.APService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class InboxControllerImpl(private val apService: APService) : InboxController {
    override suspend fun inbox(@RequestBody string: String): ResponseEntity<Unit> {
        val parseActivity = apService.parseActivity(string)
        LOGGER.info("INBOX Processing Activity Type: {}", parseActivity)
        apService.processActivity(string, parseActivity)
        return ResponseEntity(HttpStatus.ACCEPTED)
    }

    companion object {
        val LOGGER = LoggerFactory.getLogger(InboxControllerImpl::class.java)
    }
}
