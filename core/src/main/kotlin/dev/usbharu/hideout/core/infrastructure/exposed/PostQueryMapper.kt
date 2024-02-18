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

import dev.usbharu.hideout.application.infrastructure.exposed.QueryMapper
import dev.usbharu.hideout.application.infrastructure.exposed.ResultRowMapper
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Posts
import dev.usbharu.hideout.core.infrastructure.exposedrepository.PostsEmojis
import dev.usbharu.hideout.core.infrastructure.exposedrepository.PostsMedia
import org.jetbrains.exposed.sql.Query
import org.springframework.stereotype.Component

@Component
class PostQueryMapper(private val postResultRowMapper: ResultRowMapper<Post>) : QueryMapper<Post> {
    override fun map(query: Query): List<Post> {
        return query.groupBy { it[Posts.id] }
            .map { it.value }
            .map {
                it.first().let(postResultRowMapper::map)
                    .copy(
                        mediaIds = it.mapNotNull { resultRow -> resultRow.getOrNull(PostsMedia.mediaId) },
                        emojiIds = it.mapNotNull { resultRow -> resultRow.getOrNull(PostsEmojis.emojiId) }
                    )
            }
    }
}
