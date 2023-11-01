package dev.usbharu.hideout.activitypub.infrastructure.exposedquery

import dev.usbharu.hideout.activitypub.domain.model.Document
import dev.usbharu.hideout.activitypub.domain.model.Note
import dev.usbharu.hideout.activitypub.query.NoteQueryService
import dev.usbharu.hideout.activitypub.service.`object`.note.APNoteServiceImpl.Companion.public
import dev.usbharu.hideout.application.infrastructure.exposed.QueryMapper
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.infrastructure.exposedrepository.*
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class NoteQueryServiceImpl(private val postRepository: PostRepository, private val postQueryMapper: QueryMapper<Post>) :
    NoteQueryService {
    override suspend fun findById(id: Long): Pair<Note, Post> {
        return Posts
            .leftJoin(Users)
            .leftJoin(PostsMedia)
            .leftJoin(Media)
            .select { Posts.id eq id }
            .let { it.toNote() to postQueryMapper.map(it).first() }
    }

    private suspend fun ResultRow.toNote(mediaList: List<dev.usbharu.hideout.core.domain.model.media.Media>): Note {
        val replyId = this[Posts.replyId]
        val replyTo = if (replyId != null) {
            postRepository.findById(replyId).url
        } else {
            null
        }

        val visibility1 =
            visibility(
                Visibility.values().first { visibility -> visibility.ordinal == this[Posts.visibility] },
                this[Users.followers]
            )
        return Note(
            name = "Post",
            id = this[Posts.apId],
            attributedTo = this[Users.url],
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
            .map { it.first().toNote(it.mapNotNull { it.toMediaOrNull() }) }
            .first()
    }

    private fun visibility(visibility: Visibility, followers: String?): Pair<List<String>, List<String>> {
        return when (visibility) {
            Visibility.PUBLIC -> listOf(public) to listOf(public)
            Visibility.UNLISTED -> listOfNotNull(followers) to listOf(public)
            Visibility.FOLLOWERS -> listOfNotNull(followers) to listOfNotNull(followers)
            Visibility.DIRECT -> TODO()
        }
    }
}
