package dev.usbharu.hideout.service.api

import dev.usbharu.hideout.domain.model.ap.Note

interface NoteApApiService {
    suspend fun getNote(postId: Long, userId: Long?): Note?
}
