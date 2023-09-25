package dev.usbharu.hideout.controller

import dev.usbharu.hideout.domain.model.ap.Person
import dev.usbharu.hideout.service.ap.APUserService
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class UserAPControllerImpl(private val apUserService: APUserService) : UserAPController {
    override fun userAp(username: String): ResponseEntity<Person> = runBlocking {
        val person = apUserService.getPersonByName(username)
        person.context += listOf("https://www.w3.org/ns/activitystreams")
        ResponseEntity(person, HttpStatus.OK)
    }
}
