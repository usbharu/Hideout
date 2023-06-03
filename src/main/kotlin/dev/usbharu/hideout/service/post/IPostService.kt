package dev.usbharu.hideout.service.post

import dev.usbharu.hideout.domain.model.ap.Note
import dev.usbharu.hideout.domain.model.hideout.dto.PostCreateDto
import dev.usbharu.hideout.domain.model.hideout.entity.Post

interface IPostService {
    suspend fun createLocal(post: PostCreateDto): Post
    suspend fun createRemote(note: Note): Post
}
