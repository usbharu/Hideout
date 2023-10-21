package dev.usbharu.hideout.query.activitypub

import dev.usbharu.hideout.domain.model.ap.Note
import dev.usbharu.hideout.domain.model.hideout.entity.Post

interface NoteQueryService {
    suspend fun findById(id: Long): Pair<Note, Post>
}
