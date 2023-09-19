package dev.usbharu.hideout.controller

import dev.usbharu.hideout.service.ap.APService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class InboxControllerImpl(private val apService: APService) : InboxController {
    override suspend fun inbox(@RequestBody string: String): ResponseEntity<Unit> {
        val parseActivity = apService.parseActivity(string)
        apService.processActivity(string, parseActivity)
        return ResponseEntity(HttpStatus.ACCEPTED)
    }
}
