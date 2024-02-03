package dev.usbharu.hideout.activitypub.infrastructure.exposedquery

import dev.usbharu.hideout.activitypub.domain.model.Announce
import dev.usbharu.hideout.activitypub.query.AnnounceQueryService
import dev.usbharu.hideout.activitypub.service.objects.note.APNoteServiceImpl
import dev.usbharu.hideout.application.infrastructure.exposed.ResultRowMapper
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Actors
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Posts
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class ExposedAnnounceQueryService(
    private val postRepository: PostRepository,
    private val postResultRowMapper: ResultRowMapper<Post>
) : AnnounceQueryService {
    override suspend fun findById(id: Long): Pair<Announce, Post>? {
        return Posts
            .leftJoin(Actors)
            .select { Posts.id eq id }
            .singleOrNull()
            ?.let { (it.toAnnounce() ?: return null) to (postResultRowMapper.map(it)) }
    }

    override suspend fun findByApId(apId: String): Pair<Announce, Post>? {
        return Posts
            .leftJoin(Actors)
            .select { Posts.apId eq apId }
            .singleOrNull()
            ?.let { (it.toAnnounce() ?: return null) to (postResultRowMapper.map(it)) }
    }

    private suspend fun ResultRow.toAnnounce(): Announce? {
        val repostId = this[Posts.repostId] ?: return null
        val repost = postRepository.findById(repostId)?.url ?: return null

        val (to, cc) = visibility(
            Visibility.values().first { visibility -> visibility.ordinal == this[Posts.visibility] },
            this[Actors.followers]
        )

        return Announce(
            type = emptyList(),
            id = this[Posts.apId],
            apObject = repost,
            actor = this[Actors.url],
            published = Instant.ofEpochMilli(this[Posts.createdAt]).toString(),
            to = to,
            cc = cc
        )
    }

    private fun visibility(visibility: Visibility, followers: String?): Pair<List<String>, List<String>> {
        return when (visibility) {
            Visibility.PUBLIC -> listOf(APNoteServiceImpl.public) to listOf(APNoteServiceImpl.public)
            Visibility.UNLISTED -> listOfNotNull(followers) to listOf(APNoteServiceImpl.public)
            Visibility.FOLLOWERS -> listOfNotNull(followers) to listOfNotNull(followers)
            Visibility.DIRECT -> TODO()
        }
    }
}
