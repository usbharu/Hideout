package dev.usbharu.hideout.query

import dev.usbharu.hideout.domain.model.hideout.entity.Post

interface PostQueryService {
    suspend fun findById(id: Long): Post
    suspend fun findByUrl(url: String): Post
    suspend fun findByApId(string: String): Post
}
