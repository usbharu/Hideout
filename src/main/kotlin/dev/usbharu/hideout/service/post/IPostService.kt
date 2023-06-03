package dev.usbharu.hideout.service.post

import dev.usbharu.hideout.domain.model.ap.Note
import dev.usbharu.hideout.domain.model.hideout.entity.Post

interface IPostService {
    suspend fun createLocal(post: Post): Post
    suspend fun createRemote(note: Note): Post
}
