package dev.usbharu.hideout.core.domain.model.support.postdetail

import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.post.Post

data class PostDetail(
    val post: Post,
    val reply: Post? = null,
    val repost: Post? = null,
    val postActor: Actor,
    val replyActor: Actor? = null,
    val repostActor: Actor? = null
) {
    init {
        require(post.replyId == reply?.id)
        require(post.repostId == repost?.id)

        require(post.actorId == postActor.id)
        require(reply?.actorId == replyActor?.id)
        require(repost?.actorId == repostActor?.id)
    }
}
