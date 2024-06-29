package dev.usbharu.hideout.core.domain.model.timelineobject

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.emoji.EmojiId
import dev.usbharu.hideout.core.domain.model.media.MediaId
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.post.Visibility
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
    val warnFilters: List<TimelineObjectWarnFilter>
) {
}