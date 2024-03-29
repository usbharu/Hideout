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

package dev.usbharu.hideout.activitypub.infrastructure.exposedquery

import dev.usbharu.hideout.activitypub.domain.model.Document
import dev.usbharu.hideout.activitypub.domain.model.Note
import dev.usbharu.hideout.activitypub.query.NoteQueryService
import dev.usbharu.hideout.activitypub.service.objects.note.APNoteServiceImpl.Companion.public
import dev.usbharu.hideout.application.infrastructure.exposed.QueryMapper
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.infrastructure.exposedrepository.*
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class NoteQueryServiceImpl(private val postRepository: PostRepository, private val postQueryMapper: QueryMapper<Post>) :
    NoteQueryService {
    override suspend fun findById(id: Long): Pair<Note, Post>? {
        return Posts
            .leftJoin(Actors)
            .leftJoin(PostsMedia)
            .leftJoin(Media)
            .selectAll().where { Posts.id eq id }
            .let {
                (it.toNote() ?: return null) to (
                        postQueryMapper.map(it)
                            .singleOrNull() ?: return null
                        )
            }
    }

    override suspend fun findByApid(apId: String): Pair<Note, Post>? {
        return Posts
            .leftJoin(Actors)
            .leftJoin(PostsMedia)
            .leftJoin(Media)
            .selectAll().where { Posts.apId eq apId }
            .let {
                (it.toNote() ?: return null) to (
                        postQueryMapper.map(it)
                            .singleOrNull() ?: return null
                        )
            }
    }

    private suspend fun ResultRow.toNote(mediaList: List<dev.usbharu.hideout.core.domain.model.media.Media>): Note {
        val replyId = this[Posts.replyId]
        val replyTo = if (replyId != null) {
            val url = postRepository.findById(replyId)?.url
            if (url == null) {
                logger.warn("Failed to get replyId: $replyId")
            }
            url
        } else {
            null
        }

        val repostId = this[Posts.repostId]
        val repost = if (repostId != null) {
            val url = postRepository.findById(repostId)?.url
            if (url == null) {
                logger.warn("Failed to get repostId: $repostId")
            }
            url
        } else {
            null
        }

        val visibility1 =
            visibility(
                Visibility.values().first { visibility -> visibility.ordinal == this[Posts.visibility] },
                this[Actors.followers]
            )
        return Note(
            id = this[Posts.apId],
            attributedTo = this[Actors.url],
            content = this[Posts.text],
            published = Instant.ofEpochMilli(this[Posts.createdAt]).toString(),
            to = visibility1.first,
            cc = visibility1.second,
            inReplyTo = replyTo,
            misskeyQuote = repost,
            quoteUri = repost,
            quoteUrl = repost,
            sensitive = this[Posts.sensitive],
            attachment = mediaList.map { Document(url = it.url, mediaType = "image/jpeg") }
        )
    }

    private suspend fun Query.toNote(): Note? {
        return this.groupBy { it[Posts.id] }
            .map { it.value }
            .map { it.first().toNote(it.mapNotNull { resultRow -> resultRow.toMediaOrNull() }) }
            .singleOrNull()
    }

    private fun visibility(visibility: Visibility, followers: String?): Pair<List<String>, List<String>> {
        return when (visibility) {
            Visibility.PUBLIC -> listOf(public) to listOf(public)
            Visibility.UNLISTED -> listOfNotNull(followers) to listOf(public)
            Visibility.FOLLOWERS -> listOfNotNull(followers) to listOfNotNull(followers)
            Visibility.DIRECT -> TODO()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(NoteQueryServiceImpl::class.java)
    }
}
