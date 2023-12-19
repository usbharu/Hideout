package dev.usbharu.hideout.core.query

import dev.usbharu.hideout.core.domain.model.post.Post
import org.springframework.stereotype.Repository

@Repository
interface PostQueryService {
    suspend fun findById(id: Long): Post?
    suspend fun findByUrl(url: String): Post?
    suspend fun findByApId(string: String): Post?
    suspend fun findByActorId(actorId: Long): List<Post>
}
