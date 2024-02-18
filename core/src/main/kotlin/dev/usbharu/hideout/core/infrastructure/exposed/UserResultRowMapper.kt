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

package dev.usbharu.hideout.core.infrastructure.exposed

import dev.usbharu.hideout.application.infrastructure.exposed.ResultRowMapper
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Actors
import org.jetbrains.exposed.sql.ResultRow
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class UserResultRowMapper(private val actorBuilder: Actor.UserBuilder) : ResultRowMapper<Actor> {
    override fun map(resultRow: ResultRow): Actor {
        return actorBuilder.of(
            id = resultRow[Actors.id],
            name = resultRow[Actors.name],
            domain = resultRow[Actors.domain],
            screenName = resultRow[Actors.screenName],
            description = resultRow[Actors.description],
            inbox = resultRow[Actors.inbox],
            outbox = resultRow[Actors.outbox],
            url = resultRow[Actors.url],
            publicKey = resultRow[Actors.publicKey],
            privateKey = resultRow[Actors.privateKey],
            createdAt = Instant.ofEpochMilli((resultRow[Actors.createdAt])),
            keyId = resultRow[Actors.keyId],
            followers = resultRow[Actors.followers],
            following = resultRow[Actors.following],
            instance = resultRow[Actors.instance],
            locked = resultRow[Actors.locked],
            followingCount = resultRow[Actors.followingCount],
            followersCount = resultRow[Actors.followersCount],
            postsCount = resultRow[Actors.postsCount],
            lastPostDate = resultRow[Actors.lastPostAt],
            emojis = resultRow[Actors.emojis].split(",").filter { it.isNotEmpty() }.map { it.toLong() }
        )
    }
}
