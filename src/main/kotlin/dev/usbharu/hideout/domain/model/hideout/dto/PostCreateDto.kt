package dev.usbharu.hideout.domain.model.hideout.dto

import dev.usbharu.hideout.domain.model.hideout.entity.Visibility

data class PostCreateDto(
    val text: String,
    val overview: String? = null,
    val visibility: Visibility = Visibility.PUBLIC,
    val repostId: Long? = null,
    val repolyId: Long? = null,
    val userId: Long,
    val mediaIds: List<Long> = emptyList()
)
