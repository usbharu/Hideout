package dev.usbharu.hideout.core.service.post

import dev.usbharu.hideout.core.domain.model.post.Visibility

data class PostCreateDto(
    val text: String,
    val overview: String? = null,
    val visibility: Visibility = Visibility.PUBLIC,
    val repostId: Long? = null,
    val repolyId: Long? = null,
    val userId: Long,
    val mediaIds: List<Long> = emptyList()
)
