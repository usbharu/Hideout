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
        val parseActivity = try {
            apService.parseActivity(string)
        } catch (e: Exception) {
            LOGGER.warn("FAILED Parse Activity", e)
            return ResponseEntity.accepted().build()
        }
        LOGGER.info("INBOX Processing Activity Type: {}", parseActivity)
        try {
            apService.processActivity(string, parseActivity)
        } catch (e: Exception) {
            LOGGER.warn("FAILED Process Activity $parseActivity", e)
            return ResponseEntity(HttpStatus.ACCEPTED)
        }
        LOGGER.info("SUCCESS Processing Activity Type: {}", parseActivity)
        return ResponseEntity(HttpStatus.ACCEPTED)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(InboxControllerImpl::class.java)
    }
}
