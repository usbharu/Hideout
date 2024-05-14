/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.usbharu.hideout.activitypub.interfaces.api.actor

import dev.usbharu.hideout.activitypub.domain.model.Person
import dev.usbharu.hideout.activitypub.domain.model.StringOrObject
import dev.usbharu.hideout.activitypub.service.objects.user.APUserService
import dev.usbharu.hideout.core.domain.exception.resource.UserNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class UserAPControllerImpl(private val apUserService: APUserService) : UserAPController {
    override suspend fun userAp(username: String): ResponseEntity<Person> {
        val person = try {
            apUserService.getPersonByName(username)
        } catch (_: UserNotFoundException) {
            return ResponseEntity.notFound().build()
        }
        person.context += listOf(StringOrObject("https://www.w3.org/ns/activitystreams"))
        return ResponseEntity(person, HttpStatus.OK)
    }
}
