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

package utils

import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.application.config.CharacterLimit
import dev.usbharu.hideout.application.service.id.TwitterSnowflakeIdGenerateService
import dev.usbharu.hideout.core.domain.model.actor.Actor
import jakarta.validation.Validation
import kotlinx.coroutines.runBlocking
import java.net.URL
import java.time.Instant

object UserBuilder {
    private val actorBuilder = Actor.UserBuilder(
        CharacterLimit(), ApplicationConfig(URL("https://example.com")),
        Validation.buildDefaultValidatorFactory().validator
    )

    private val idGenerator = TwitterSnowflakeIdGenerateService

    fun localUserOf(
        id: Long = generateId(),
        name: String = "test-user-$id",
        domain: String = "example.com",
        screenName: String = name,
        description: String = "This user is test user.",
        inbox: String = "https://$domain/users/$id/inbox",
        outbox: String = "https://$domain/users/$id/outbox",
        url: String = "https://$domain/users/$id",
        publicKey: String = "-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----",
        privateKey: String = "-----BEGIN PRIVATE KEY-----...-----END PRIVATE KEY-----",
        createdAt: Instant = Instant.now(),
        keyId: String = "https://$domain/users/$id#pubkey",
        followers: String = "https://$domain/users/$id/followers",
        following: String = "https://$domain/users/$id/following",
    ): Actor {
        return actorBuilder.of(
            id = id,
            name = name,
            domain = domain,
            screenName = screenName,
            description = description,
            inbox = inbox,
            outbox = outbox,
            url = url,
            publicKey = publicKey,
            privateKey = privateKey,
            createdAt = createdAt,
            keyId = keyId,
            followers = followers,
            following = following,
            locked = false,
            instance = 0
        )
    }

    fun remoteUserOf(
        id: Long = generateId(),
        name: String = "test-user-$id",
        domain: String = "remote.example.com",
        screenName: String = name,
        description: String = "This user is test user.",
        inbox: String = "https://$domain/$id/inbox",
        outbox: String = "https://$domain/$id/outbox",
        url: String = "https://$domain/$id/",
        publicKey: String = "-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----",
        createdAt: Instant = Instant.now(),
        keyId: String = "https://$domain/$id#pubkey",
        followers: String = "https://$domain/$id/followers",
        following: String = "https://$domain/$id/following",
        instanceId: Long = generateId(),
    ): Actor {
        return actorBuilder.of(
            id = id,
            name = name,
            domain = domain,
            screenName = screenName,
            description = description,
            inbox = inbox,
            outbox = outbox,
            url = url,
            publicKey = publicKey,
            privateKey = null,
            createdAt = createdAt,
            keyId = keyId,
            followers = followers,
            following = following,
            locked = false,
            instance = instanceId
        )
    }

    private fun generateId(): Long = runBlocking {
        idGenerator.generateId()
    }
}
