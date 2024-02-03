package dev.usbharu.hideout.activitypub.query

import dev.usbharu.hideout.activitypub.domain.model.Announce
import dev.usbharu.hideout.core.domain.model.post.Post
import org.springframework.stereotype.Repository

@Repository
interface AnnounceQueryService {
    suspend fun findById(id: Long): Pair<Announce, Post>?
    suspend fun findByApId(apId: String): Pair<Announce, Post>?
}
