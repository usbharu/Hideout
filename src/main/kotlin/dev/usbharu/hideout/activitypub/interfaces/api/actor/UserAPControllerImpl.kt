package dev.usbharu.hideout.activitypub.interfaces.api.actor

import dev.usbharu.hideout.activitypub.domain.model.Person
import dev.usbharu.hideout.activitypub.service.objects.user.APUserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class UserAPControllerImpl(private val apUserService: APUserService) : UserAPController {
    override suspend fun userAp(username: String): ResponseEntity<Person> {
        val person = apUserService.getPersonByName(username)
        person.context += listOf("https://www.w3.org/ns/activitystreams")
        return ResponseEntity(person, HttpStatus.OK)
    }
}
