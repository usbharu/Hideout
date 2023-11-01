package dev.usbharu.hideout.activitypub.service.`object`.note

import dev.usbharu.hideout.activitypub.domain.model.Note

interface NoteApApiService {
    suspend fun getNote(postId: Long, userId: Long?): Note?
}
