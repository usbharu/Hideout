package dev.usbharu.hideout.controller

import dev.usbharu.hideout.domain.model.ap.Person
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
interface UserAPController {
    @GetMapping("/users/{username}")
    fun userAp(@PathVariable("username") username: String): ResponseEntity<Person>
}
