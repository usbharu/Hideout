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

package dev.usbharu.hideout.core.application.actor

import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.emoji.CustomEmoji
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetail
import java.time.Instant

data class UserDetail(
    val id: Long,
    val userDetailId: Long,
    val name: String,
    val domain: String,
    val screenName: String,
    val url: String,
    val iconUrl: String,
    val description: String,
    val locked: Boolean,
    val emojis: List<CustomEmoji>,
    val createdAt: Instant,
    val lastPostAt: Instant?,
    val postsCount: Int,
    val followingCount: Int?,
    val followersCount: Int?,
    val moveTo: Long?,
    val suspend: Boolean,
) {
    companion object {
        fun of(
            actor: Actor,
            userDetail: UserDetail,
            customEmojis: List<CustomEmoji>,
        ): dev.usbharu.hideout.core.application.actor.UserDetail {
            return UserDetail(
                actor.id.id,
                userDetail.id.id,
                actor.name.name,
                actor.domain.domain,
                actor.screenName.screenName,
                actor.url.toString(),
                actor.url.toString(),
                actor.description.description,
                actor.locked,
                customEmojis,
                actor.createdAt,
                actor.lastPostAt,
                actor.postsCount.postsCount,
                actor.followingCount?.relationshipCount,
                actor.followersCount?.relationshipCount,
                actor.moveTo?.id,
                actor.suspend
            )
        }
    }
}
