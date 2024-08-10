package dev.usbharu.hideout.core.domain.model.support.timelineobjectdetail

import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.timelineobject.TimelineObject
import dev.usbharu.hideout.core.domain.model.timelineobject.TimelineObjectId
import dev.usbharu.hideout.core.domain.model.timelineobject.TimelineObjectWarnFilter
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetail
import java.time.Instant

data class TimelineObjectDetail(
    val id: TimelineObjectId,
    val postId: PostId,
    val timelineUserDetail: UserDetail,
    val post: Post,
    val postActor: Actor,
    val replyPost: Post?,
    val replyPostActor: Actor?,
    val repostPost: Post?,
    val repostPostActor: Actor?,
    val isPureRepost: Boolean,
    val lastUpdateAt: Instant,
    val hasMediaInRepost: Boolean,
    val warnFilter: List<TimelineObjectWarnFilter>
) {
    companion object {
        fun of(
            timelineObject: TimelineObject,
            timelineUserDetail: UserDetail,
            post: Post,
            postActor: Actor,
            replyPost: Post?,
            replyPostActor: Actor?,
            repostPost: Post?,
            repostPostActor: Actor?,
            warnFilter: List<TimelineObjectWarnFilter>
        ): TimelineObjectDetail {
            return TimelineObjectDetail(
                timelineObject.id,
                post.id,
                timelineUserDetail,
                post,
                postActor,
                replyPost,
                replyPostActor,
                repostPost,
                repostPostActor,
                timelineObject.isPureRepost,
                timelineObject.lastUpdatedAt,
                timelineObject.hasMediaInRepost,
                warnFilter
            )
        }
    }
}
