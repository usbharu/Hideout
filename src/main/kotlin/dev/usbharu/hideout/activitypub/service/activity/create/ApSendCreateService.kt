package dev.usbharu.hideout.activitypub.service.activity.create

import dev.usbharu.hideout.core.domain.model.post.Post

interface ApSendCreateService {
    suspend fun createNote(post: Post)
}
