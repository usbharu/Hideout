package dev.usbharu.hideout.domain.model.hideout.form

import dev.usbharu.hideout.domain.model.hideout.entity.Visibility

data class Post(
    val text: String,
    val overview: String? = null,
    val visibility: Visibility = Visibility.PUBLIC,
    val repostId: Long? = null,
    val replyId: Long? = null
)
