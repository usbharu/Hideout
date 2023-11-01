package dev.usbharu.hideout.activitypub.interfaces.api.actor

import dev.usbharu.hideout.activitypub.domain.model.Person
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
interface UserAPController {
    @GetMapping("/users/{username}")
    suspend fun userAp(@PathVariable("username") username: String): ResponseEntity<Person>
}
