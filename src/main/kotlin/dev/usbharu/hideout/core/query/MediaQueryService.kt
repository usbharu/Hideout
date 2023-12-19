package dev.usbharu.hideout.core.query

import dev.usbharu.hideout.core.domain.model.media.Media

interface MediaQueryService {
    suspend fun findByPostId(postId: Long): List<Media>
    suspend fun findByRemoteUrl(remoteUrl: String): Media?
}
