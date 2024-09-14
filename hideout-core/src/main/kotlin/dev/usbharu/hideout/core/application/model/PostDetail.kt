package dev.usbharu.hideout.core.application.model

import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.media.Media
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.Visibility
import java.net.URI
import java.time.Instant

data class PostDetail(
    val id: Long,
    val actor: ActorDetail,
    val overview: String?,
    val text: String,
    val content: String,
    val createdAt: Instant,
    val visibility: Visibility,
    val pureRepost: Boolean,
    val url: URI,
    val apId: URI,
    val repost: PostDetail?,
    val reply: PostDetail?,
    val sensitive: Boolean,
    val deleted: Boolean,
    val mediaDetailList: List<MediaDetail>,
    val moveTo: PostDetail?,
    val reactionsList: List<Reactions>,
    val favourited: Boolean
) {
    companion object {
        @Suppress("LongParameterList")
        fun of(
            post: Post,
            actor: Actor,
            iconMedia: Media?,
            mediaList: List<Media>,
            reply: PostDetail? = null,
            repost: PostDetail? = null,
            moveTo: PostDetail? = null,
            reactionsList: List<Reactions>,
            favourited: Boolean
        ): PostDetail {
            return PostDetail(
                id = post.id.id,
                actor = ActorDetail.of(actor, iconMedia, null),
                overview = post.overview?.overview,
                text = post.text,
                content = post.content.content,
                createdAt = post.createdAt,
                visibility = post.visibility,
                pureRepost = post.isPureRepost,
                url = post.url,
                apId = post.apId,
                repost = repost,
                reply = reply,
                sensitive = post.sensitive,
                deleted = false,
                mediaDetailList = mediaList.map { MediaDetail.of(it) },
                moveTo = moveTo,
                reactionsList = reactionsList,
                favourited = favourited
            )
        }
    }
}
