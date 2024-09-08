package dev.usbharu.hideout.core.query.reactions

import dev.usbharu.hideout.core.application.model.Reactions
import dev.usbharu.hideout.core.domain.model.post.PostId

interface ReactionsQueryService {
    suspend fun findAllByPostId(postId: PostId): List<Reactions>
}