package dev.usbharu.hideout.query.activitypub

import dev.usbharu.hideout.domain.model.ap.Note
import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.exception.FailedToGetResourcesException
import dev.usbharu.hideout.repository.Posts
import dev.usbharu.hideout.repository.Users
import dev.usbharu.hideout.repository.toPost
import dev.usbharu.hideout.util.singleOr
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class NoteQueryServiceImpl : NoteQueryService {
    override suspend fun findById(id: Long): Pair<Note, Post> {
        return Posts
            .leftJoin(Users)
            .select { Posts.id eq id }
            .singleOr { FailedToGetResourcesException("id $id is duplicate or does not exist.") }
            .let { it.toNote() to it.toPost() }
    }

    private fun ResultRow.toNote(): Note {
        return Note(
            name = "Post",
            id = this[Posts.apId],
            attributedTo = this[Users.url],
            content = this[Posts.text],
            published = Instant.ofEpochMilli(this[Posts.createdAt]).toString(),
            to = listOf(),
            cc = listOf(),
            inReplyTo = null,
            sensitive = false
        )
    }
}
