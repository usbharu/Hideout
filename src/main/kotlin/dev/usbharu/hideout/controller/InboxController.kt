package dev.usbharu.hideout.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
interface InboxController {
    @RequestMapping(
        "/inbox",
        "/users/{username}/inbox",
        produces = ["application/activity+json", "application/ld+json; profile=\"https://www.w3.org/ns/activitystreams\""],
        method = [RequestMethod.GET, RequestMethod.POST]
    )
    fun inbox(@RequestBody string: String): ResponseEntity<Unit> {
        return ResponseEntity(HttpStatus.ACCEPTED)
    }
}
