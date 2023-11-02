package dev.usbharu.hideout.core.service.timeline

import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.domain.model.timeline.Timeline
import dev.usbharu.hideout.core.domain.model.timeline.TimelineRepository
import dev.usbharu.hideout.core.query.FollowerQueryService
import dev.usbharu.hideout.core.query.UserQueryService
import org.springframework.stereotype.Service

@Service
class TimelineService(
    private val followerQueryService: FollowerQueryService,
    private val userQueryService: UserQueryService,
    private val timelineRepository: TimelineRepository
) {
    suspend fun publishTimeline(post: Post, isLocal: Boolean) {
        val findFollowersById = followerQueryService.findFollowersById(post.userId).toMutableList()
        if (isLocal) {
            // 自分自身も含める必要がある
            val user = userQueryService.findById(post.userId)
            findFollowersById.add(user)
        }
        val timelines = findFollowersById.map {
            Timeline(
                id = timelineRepository.generateId(),
                userId = it.id,
                timelineId = 0,
                postId = post.id,
                postUserId = post.userId,
                createdAt = post.createdAt,
                replyId = post.replyId,
                repostId = post.repostId,
                visibility = post.visibility,
                sensitive = post.sensitive,
                isLocal = isLocal,
                isPureRepost = post.repostId == null || (post.text.isBlank() && post.overview.isNullOrBlank()),
                mediaIds = post.mediaIds
            )
        }.toMutableList()
        if (post.visibility == Visibility.PUBLIC) {
            timelines.add(
                Timeline(
                    id = timelineRepository.generateId(),
                    userId = 0,
                    timelineId = 0,
                    postId = post.id,
                    postUserId = post.userId,
                    createdAt = post.createdAt,
                    replyId = post.replyId,
                    repostId = post.repostId,
                    visibility = post.visibility,
                    sensitive = post.sensitive,
                    isLocal = isLocal,
                    isPureRepost = post.repostId == null || (post.text.isBlank() && post.overview.isNullOrBlank()),
                    mediaIds = post.mediaIds
                )
            )
        }
        timelineRepository.saveAll(timelines)
    }
}