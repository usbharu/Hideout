package dev.usbharu.hideout.query

import dev.usbharu.hideout.domain.model.hideout.entity.Media

interface MediaQueryService {
    suspend fun findByPostId(postId: Long): List<Media>
}
