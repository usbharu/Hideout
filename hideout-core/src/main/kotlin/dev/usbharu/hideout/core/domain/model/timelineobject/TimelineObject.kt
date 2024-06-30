package dev.usbharu.hideout.core.domain.model.timelineobject

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.emoji.EmojiId
import dev.usbharu.hideout.core.domain.model.filter.FilterResult
import dev.usbharu.hideout.core.domain.model.media.MediaId
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.PostContent
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.domain.model.timeline.Timeline
import dev.usbharu.hideout.core.domain.model.timeline.TimelineId
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import java.time.Instant

class TimelineObject(
    val id: TimelineObjectId,
    val userDetailId: UserDetailId,
    val timelineId: TimelineId,
    val postId: PostId,
    val postActorId: ActorId,
    val postCreatedAt: Instant,
    val replyId: PostId?,
    val repostId: PostId?,
    val visibility: Visibility,
    val isPureRepost: Boolean,
    val mediaIds: List<MediaId>,
    val emojiIds: List<EmojiId>,
    val visibleActors: List<ActorId>,
    val hasMedia: Boolean,
    val hasMediaInRepost: Boolean,
    val lastUpdatedAt: Instant,
    val warnFilters: List<TimelineObjectWarnFilter>,
) {
    companion object {

        fun create(
            timelineObjectId: TimelineObjectId,
            timeline: Timeline,
            post: Post,
            filterResults: List<FilterResult>
        ): TimelineObject {
            return TimelineObject(
                id = timelineObjectId,
                userDetailId = timeline.userDetailId,
                timelineId = timeline.id,
                postId = post.id,
                postActorId = post.actorId,
                postCreatedAt = post.createdAt,
                replyId = post.replyId,
                repostId = null,
                visibility = post.visibility,
                isPureRepost = true,
                mediaIds = post.mediaIds,
                emojiIds = post.emojiIds,
                visibleActors = post.visibleActors.toList(),
                hasMedia = post.mediaIds.isNotEmpty(),
                hasMediaInRepost = false,
                lastUpdatedAt = Instant.now(),
                warnFilters = filterResults.map { TimelineObjectWarnFilter(it.filter.id, it.matchedKeyword) }
            )
        }

        fun create(
            timelineObjectId: TimelineObjectId,
            timeline: Timeline,
            post: Post,
            repost: Post,
            filterResults: List<FilterResult>
        ): TimelineObject {

            require(post.repostId == repost.id)

            return TimelineObject(
                id = timelineObjectId,
                userDetailId = timeline.userDetailId,
                timelineId = timeline.id,
                postId = post.id,
                postActorId = post.actorId,
                postCreatedAt = post.createdAt,
                replyId = post.replyId,
                repostId = repost.id,
                visibility = post.visibility,
                isPureRepost = repost.mediaIds.isEmpty() &&
                        repost.overview == null &&
                        repost.content == PostContent.empty &&
                        repost.replyId == null,
                mediaIds = post.mediaIds,
                emojiIds = post.emojiIds,
                visibleActors = post.visibleActors.toList(),
                hasMedia = post.mediaIds.isNotEmpty(),
                hasMediaInRepost = repost.mediaIds.isNotEmpty(),
                lastUpdatedAt = Instant.now(),
                warnFilters = filterResults.map { TimelineObjectWarnFilter(it.filter.id, it.matchedKeyword) }
            )
        }
    }
}