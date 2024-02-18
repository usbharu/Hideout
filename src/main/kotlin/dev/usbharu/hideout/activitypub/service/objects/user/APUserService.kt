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

package dev.usbharu.hideout.activitypub.service.objects.user

import dev.usbharu.hideout.activitypub.domain.model.Image
import dev.usbharu.hideout.activitypub.domain.model.Key
import dev.usbharu.hideout.activitypub.domain.model.Person
import dev.usbharu.hideout.activitypub.service.common.APResourceResolveService
import dev.usbharu.hideout.activitypub.service.common.resolve
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.exception.resource.UserNotFoundException
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.service.user.RemoteUserCreateDto
import dev.usbharu.hideout.core.service.user.UserService
import org.springframework.stereotype.Service

interface APUserService {
    suspend fun getPersonByName(name: String): Person

    /**
     * Fetch person
     *
     * @param url
     * @param targetActor 署名するユーザー
     * @return
     */
    suspend fun fetchPerson(url: String, targetActor: String? = null): Person

    suspend fun fetchPersonWithEntity(url: String, targetActor: String? = null): Pair<Person, Actor>
}

@Service
class APUserServiceImpl(
    private val userService: UserService,
    private val transaction: Transaction,
    private val applicationConfig: ApplicationConfig,
    private val apResourceResolveService: APResourceResolveService,
    private val actorRepository: ActorRepository
) :
    APUserService {

    override suspend fun getPersonByName(name: String): Person {
        val userEntity = transaction.transaction {
            actorRepository.findByNameAndDomain(name, applicationConfig.url.host)
                ?: throw UserNotFoundException.withNameAndDomain(name, applicationConfig.url.host)
        }
        // TODO: JOINで書き直し
        val userUrl = "${applicationConfig.url}/users/$name"
        return Person(
            type = emptyList(),
            name = userEntity.name,
            id = userUrl,
            preferredUsername = name,
            summary = userEntity.description,
            inbox = "$userUrl/inbox",
            outbox = "$userUrl/outbox",
            url = userUrl,
            icon = Image(
                type = emptyList(),
                mediaType = "image/jpeg",
                url = "$userUrl/icon.jpg"
            ),
            publicKey = Key(
                id = userEntity.keyId,
                owner = userUrl,
                publicKeyPem = userEntity.publicKey
            ),
            endpoints = mapOf("sharedInbox" to "${applicationConfig.url}/inbox"),
            followers = userEntity.followers,
            following = userEntity.following,
            manuallyApprovesFollowers = userEntity.locked
        )
    }

    override suspend fun fetchPerson(url: String, targetActor: String?): Person =
        fetchPersonWithEntity(url, targetActor).first

    override suspend fun fetchPersonWithEntity(url: String, targetActor: String?): Pair<Person, Actor> {
        val userEntity = actorRepository.findByUrl(url)

        if (userEntity != null) {
            return entityToPerson(userEntity, userEntity.url) to userEntity
        }

        val person = apResourceResolveService.resolve<Person>(url, null as Long?)

        val id = person.id

        val actor = actorRepository.findByUrlWithLock(id)

        if (actor != null) {
            return person to actor
        }

        return person to userService.createRemoteUser(
            RemoteUserCreateDto(
                name = person.preferredUsername,
                domain = id.substringAfter("://").substringBefore("/"),
                screenName = person.name ?: person.preferredUsername,
                description = person.summary.orEmpty(),
                inbox = person.inbox,
                outbox = person.outbox,
                url = id,
                publicKey = person.publicKey.publicKeyPem,
                keyId = person.publicKey.id,
                following = person.following,
                followers = person.followers,
                sharedInbox = person.endpoints["sharedInbox"],
                locked = person.manuallyApprovesFollowers
            )
        )
    }

    private fun entityToPerson(
        actorEntity: Actor,
        id: String
    ) = Person(
        type = emptyList(),
        name = actorEntity.name,
        id = id,
        preferredUsername = actorEntity.name,
        summary = actorEntity.description,
        inbox = "$id/inbox",
        outbox = "$id/outbox",
        url = id,
        icon = Image(
            type = emptyList(),
            mediaType = "image/jpeg",
            url = "$id/icon.jpg"
        ),
        publicKey = Key(
            id = actorEntity.keyId,
            owner = id,
            publicKeyPem = actorEntity.publicKey
        ),
        endpoints = mapOf("sharedInbox" to "${applicationConfig.url}/inbox"),
        followers = actorEntity.followers,
        following = actorEntity.following,
        manuallyApprovesFollowers = actorEntity.locked
    )
}
