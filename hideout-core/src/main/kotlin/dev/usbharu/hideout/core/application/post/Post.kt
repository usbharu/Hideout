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

package dev.usbharu.hideout.core.application.post

import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.Visibility
import java.net.URI
import java.time.Instant

data class Post(
    val id: Long,
    val actorId: Long,
    val overview: String?,
    val text: String,
    val content: String,
    val createdAt: Instant,
    val visibility: Visibility,
    val url: URI,
    val repostId: Long?,
    val replyId: Long?,
    val sensitive: Boolean,
    val mediaIds: List<Long>,
    val moveTo: Long?,
) {
    companion object {
        fun of(post: Post): dev.usbharu.hideout.core.application.post.Post {
            return Post(
                id = post.id.id,
                actorId = post.actorId.id,
                overview = post.overview?.overview,
                text = post.text,
                content = post.content.content,
                createdAt = post.createdAt,
                visibility = post.visibility,
                url = post.url,
                repostId = post.repostId?.id,
                replyId = post.replyId?.id,
                sensitive = post.sensitive,
                mediaIds = post.mediaIds.map { it.id },
                moveTo = post.moveTo?.id
            )
        }
    }
}
