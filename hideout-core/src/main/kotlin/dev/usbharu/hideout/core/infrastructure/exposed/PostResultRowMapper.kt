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
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Posts
import org.jetbrains.exposed.sql.ResultRow
import org.springframework.stereotype.Component

@Component
class PostResultRowMapper(private val postBuilder: Post.PostBuilder) : ResultRowMapper<Post> {
    override fun map(resultRow: ResultRow): Post {

        return postBuilder.of(
            id = resultRow[Posts.id],
            actorId = resultRow[Posts.actorId],
            overview = resultRow[Posts.overview],
            content = resultRow[Posts.text],
            createdAt = resultRow[Posts.createdAt],
            visibility = Visibility.values().first { visibility -> visibility.ordinal == resultRow[Posts.visibility] },
            url = resultRow[Posts.url],
            repostId = resultRow[Posts.repostId],
            replyId = resultRow[Posts.replyId],
            sensitive = resultRow[Posts.sensitive],
            apId = resultRow[Posts.apId],
            deleted = resultRow[Posts.deleted],
        )
    }
}
