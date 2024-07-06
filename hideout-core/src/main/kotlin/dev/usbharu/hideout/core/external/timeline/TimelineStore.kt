package dev.usbharu.hideout.core.external.timeline

import dev.usbharu.hideout.core.domain.model.post.Post

interface TimelineStore {
    suspend fun newPost(post: Post)
}