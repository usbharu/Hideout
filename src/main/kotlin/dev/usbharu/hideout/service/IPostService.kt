package dev.usbharu.hideout.service

import dev.usbharu.hideout.domain.model.Post

interface IPostService {
    suspend fun create(post:Post)
}
