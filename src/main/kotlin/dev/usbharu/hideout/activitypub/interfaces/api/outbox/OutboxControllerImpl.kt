package dev.usbharu.hideout.activitypub.interfaces.api.outbox

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class OutboxControllerImpl : OutboxController {
    override suspend fun outbox(): ResponseEntity<Unit> =
        ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
}
