package dev.usbharu.hideout.controller

import dev.usbharu.hideout.domain.model.ap.Person
import dev.usbharu.hideout.service.ap.APUserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class UserAPControllerImpl(private val apUserService: APUserService) : UserAPController {
    override suspend fun userAp(username: String): ResponseEntity<Person> {
        val person = apUserService.getPersonByName(username)
        return ResponseEntity(person, HttpStatus.OK)
    }
}
