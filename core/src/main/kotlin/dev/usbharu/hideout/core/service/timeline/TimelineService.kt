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

package dev.usbharu.hideout.core.service.timeline

import dev.usbharu.hideout.core.domain.exception.resource.UserNotFoundException
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.domain.model.timeline.Timeline
import dev.usbharu.hideout.core.domain.model.timeline.TimelineRepository
import dev.usbharu.hideout.core.query.FollowerQueryService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TimelineService(
    private val followerQueryService: FollowerQueryService,
    private val timelineRepository: TimelineRepository,
    private val actorRepository: ActorRepository
) {
    suspend fun publishTimeline(post: Post, isLocal: Boolean) {
        val findFollowersById = followerQueryService.findFollowersById(post.actorId).toMutableList()
        if (isLocal) {
            // 自分自身も含める必要がある
            val user = actorRepository.findById(post.actorId) ?: throw UserNotFoundException.withId(post.actorId)
            findFollowersById.add(user)
        }
        val timelines = findFollowersById.map {
            Timeline(
                id = timelineRepository.generateId(),
                userId = it.id,
                timelineId = 0,
                postId = post.id,
                postActorId = post.actorId,
                createdAt = post.createdAt,
                replyId = post.replyId,
                repostId = post.repostId,
                visibility = post.visibility,
                sensitive = post.sensitive,
                isLocal = isLocal,
                isPureRepost = post.repostId == null || (post.text.isBlank() && post.overview.isNullOrBlank()),
                mediaIds = post.mediaIds,
                emojiIds = post.emojiIds
            )
        }.toMutableList()
        if (post.visibility == Visibility.PUBLIC) {
            timelines.add(
                Timeline(
                    id = timelineRepository.generateId(),
                    userId = 0,
                    timelineId = 0,
                    postId = post.id,
                    postActorId = post.actorId,
                    createdAt = post.createdAt,
                    replyId = post.replyId,
                    repostId = post.repostId,
                    visibility = post.visibility,
                    sensitive = post.sensitive,
                    isLocal = isLocal,
                    isPureRepost = post.repostId == null || (post.text.isBlank() && post.overview.isNullOrBlank()),
                    mediaIds = post.mediaIds,
                    emojiIds = post.emojiIds
                )
            )
        }
        timelineRepository.saveAll(timelines)
        logger.debug("SUCCESS Timeline published. {}", timelines.size)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TimelineService::class.java)
    }
}
