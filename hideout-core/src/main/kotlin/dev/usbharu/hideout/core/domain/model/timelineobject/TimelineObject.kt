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
    val replyActorId: ActorId?,
    val repostId: PostId?,
    val repostActorId: ActorId?,
    visibility: Visibility,
    isPureRepost: Boolean,
    mediaIds: List<MediaId>,
    emojiIds: List<EmojiId>,
    visibleActors: List<ActorId>,
    hasMediaInRepost: Boolean,
    lastUpdatedAt: Instant,
    var warnFilters: List<TimelineObjectWarnFilter>,

    ) {
    var isPureRepost = isPureRepost
        private set
    var visibleActors = visibleActors
        private set
    var hasMediaInRepost = hasMediaInRepost
        private set
    val hasMedia
        get() = mediaIds.isNotEmpty()

    var lastUpdatedAt = lastUpdatedAt
        private set
    var visibility = visibility
        private set
    var mediaIds = mediaIds
        private set
    var emojiIds = emojiIds
        private set

    fun updateWith(post: Post, filterResults: List<FilterResult>) {
        visibleActors = post.visibleActors.toList()
        visibility = post.visibility
        mediaIds = post.mediaIds.toList()
        emojiIds = post.emojiIds.toList()
        lastUpdatedAt = Instant.now()
        isPureRepost =
            post.repostId != null && post.replyId == null && post.text.isEmpty() && post.overview?.overview.isNullOrEmpty()
        warnFilters = filterResults.map { TimelineObjectWarnFilter(it.filter.id, it.matchedKeyword) }
    }

    fun updateWith(post: Post, repost: Post, filterResults: List<FilterResult>) {
        require(repost.id == post.repostId)
        require(repostId == post.repostId)

        updateWith(post, filterResults)
        hasMediaInRepost = repost.mediaIds.isNotEmpty()
    }

    companion object {

        fun create(
            timelineObjectId: TimelineObjectId,
            timeline: Timeline,
            post: Post,
            replyActorId: ActorId?,
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
                replyActorId = replyActorId,
                repostId = null,
                repostActorId = null,
                visibility = post.visibility,
                isPureRepost = true,
                mediaIds = post.mediaIds,
                emojiIds = post.emojiIds,
                visibleActors = post.visibleActors.toList(),
                hasMediaInRepost = false,
                lastUpdatedAt = Instant.now(),
                warnFilters = filterResults.map { TimelineObjectWarnFilter(it.filter.id, it.matchedKeyword) }
            )
        }

        fun create(
            timelineObjectId: TimelineObjectId,
            timeline: Timeline,
            post: Post,
            replyActorId: ActorId?,
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
                replyActorId = replyActorId,
                repostId = repost.id,
                repostActorId = repost.actorId,
                visibility = post.visibility,
                isPureRepost = repost.mediaIds.isEmpty() &&
                        repost.overview == null &&
                        repost.content == PostContent.empty &&
                        repost.replyId == null,
                mediaIds = post.mediaIds,
                emojiIds = post.emojiIds,
                visibleActors = post.visibleActors.toList(),
                hasMediaInRepost = repost.mediaIds.isNotEmpty(),
                lastUpdatedAt = Instant.now(),
                warnFilters = filterResults.map { TimelineObjectWarnFilter(it.filter.id, it.matchedKeyword) }
            )
        }
    }
}
