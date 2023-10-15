package dev.usbharu.hideout.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class OutboxControllerImpl : OutboxController {
    override suspend fun outbox(@RequestBody string: String): ResponseEntity<Unit> =
        ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
}
