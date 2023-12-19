package dev.usbharu.hideout.activitypub.infrastructure.exposedquery

import dev.usbharu.hideout.activitypub.domain.model.Document
import dev.usbharu.hideout.activitypub.domain.model.Note
import dev.usbharu.hideout.activitypub.query.NoteQueryService
import dev.usbharu.hideout.activitypub.service.objects.note.APNoteServiceImpl.Companion.public
import dev.usbharu.hideout.application.infrastructure.exposed.QueryMapper
import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.infrastructure.exposedrepository.*
import dev.usbharu.hideout.util.singleOr
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class NoteQueryServiceImpl(private val postRepository: PostRepository, private val postQueryMapper: QueryMapper<Post>) :
    NoteQueryService {
    override suspend fun findById(id: Long): Pair<Note, Post> {
        return Posts
            .leftJoin(Actors)
            .leftJoin(PostsMedia)
            .leftJoin(Media)
            .select { Posts.id eq id }
            .let {
                it.toNote() to postQueryMapper.map(it)
                    .singleOr { FailedToGetResourcesException("id: $id does not exist.") }
            }
    }

    override suspend fun findByApid(apId: String): Pair<Note, Post> {
        return Posts
            .leftJoin(Actors)
            .leftJoin(PostsMedia)
            .leftJoin(Media)
            .select { Posts.apId eq apId }
            .let {
                it.toNote() to postQueryMapper.map(it)
                    .singleOr { FailedToGetResourcesException("apid: $apId does not exist.") }
            }
    }

    private suspend fun ResultRow.toNote(mediaList: List<dev.usbharu.hideout.core.domain.model.media.Media>): Note {
        val replyId = this[Posts.replyId]
        val replyTo = if (replyId != null) {
            try {
                postRepository.findById(replyId)?.url ?: throw FailedToGetResourcesException()
            } catch (e: FailedToGetResourcesException) {
                logger.warn("Failed to get replyId: $replyId", e)
                null
            }
        } else {
            null
        }

        val visibility1 =
            visibility(
                Visibility.values().first { visibility -> visibility.ordinal == this[Posts.visibility] },
                this[Actors.followers]
            )
        return Note(
            id = this[Posts.apId],
            attributedTo = this[Actors.url],
            content = this[Posts.text],
            published = Instant.ofEpochMilli(this[Posts.createdAt]).toString(),
            to = visibility1.first,
            cc = visibility1.second,
            inReplyTo = replyTo,
            sensitive = this[Posts.sensitive],
            attachment = mediaList.map { Document(url = it.url, mediaType = "image/jpeg") }
        )
    }

    private suspend fun Query.toNote(): Note {
        return this.groupBy { it[Posts.id] }
            .map { it.value }
            .map { it.first().toNote(it.mapNotNull { resultRow -> resultRow.toMediaOrNull() }) }
            .singleOr { FailedToGetResourcesException("resource does not exist.") }
    }

    private fun visibility(visibility: Visibility, followers: String?): Pair<List<String>, List<String>> {
        return when (visibility) {
            Visibility.PUBLIC -> listOf(public) to listOf(public)
            Visibility.UNLISTED -> listOfNotNull(followers) to listOf(public)
            Visibility.FOLLOWERS -> listOfNotNull(followers) to listOfNotNull(followers)
            Visibility.DIRECT -> TODO()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(NoteQueryServiceImpl::class.java)
    }
}
