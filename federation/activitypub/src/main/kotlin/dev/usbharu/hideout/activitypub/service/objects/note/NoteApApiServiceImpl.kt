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

package dev.usbharu.hideout.activitypub.service.objects.note

import dev.usbharu.hideout.activitypub.domain.model.Note
import dev.usbharu.hideout.activitypub.query.NoteQueryService
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.query.FollowerQueryService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class NoteApApiServiceImpl(
    private val noteQueryService: NoteQueryService,
    private val followerQueryService: FollowerQueryService,
    private val transaction: Transaction
) : NoteApApiService {
    override suspend fun getNote(postId: Long, userId: Long?): Note? = transaction.transaction {
        val findById = noteQueryService.findById(postId)

        if (findById == null) {
            logger.warn("Note not found. $postId $userId")
            return@transaction null
        }

        when (findById.second.visibility) {
            Visibility.PUBLIC, Visibility.UNLISTED -> {
                return@transaction findById.first
            }

            Visibility.FOLLOWERS -> {
                return@transaction getFollowersNote(userId, findById)
            }

            Visibility.DIRECT -> return@transaction null
        }
    }

    private suspend fun getFollowersNote(
        userId: Long?,
        findById: Pair<Note, Post>
    ): Note? {
        if (userId == null) {
            return null
        }

        if (followerQueryService.alreadyFollow(findById.second.actorId, userId)) {
            return findById.first
        }
        return null
    }

    companion object {
        private val logger = LoggerFactory.getLogger(NoteApApiServiceImpl::class.java)
    }
}
