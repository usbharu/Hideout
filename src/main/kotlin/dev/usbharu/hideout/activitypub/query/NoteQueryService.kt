package dev.usbharu.hideout.activitypub.query

import dev.usbharu.hideout.activitypub.domain.model.Note
import dev.usbharu.hideout.core.domain.model.post.Post

interface NoteQueryService {
    suspend fun findById(id: Long): Pair<Note, Post>
}
