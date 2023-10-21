package dev.usbharu.hideout.query.activitypub

import dev.usbharu.hideout.domain.model.ap.Document
import dev.usbharu.hideout.domain.model.ap.Note
import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.domain.model.hideout.entity.Visibility
import dev.usbharu.hideout.repository.*
import dev.usbharu.hideout.service.ap.APNoteServiceImpl.Companion.public
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class NoteQueryServiceImpl(private val postRepository: PostRepository) : NoteQueryService {
    override suspend fun findById(id: Long): Pair<Note, Post> {
        return Posts
            .leftJoin(Users)
            .leftJoin(PostsMedia)
            .leftJoin(Media)
            .select { Posts.id eq id }
            .let { it.toNote() to it.toPost().first() }

    }

    private suspend fun ResultRow.toNote(mediaList: List<dev.usbharu.hideout.domain.model.hideout.entity.Media>): Note {
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
