package dev.usbharu.hideout.core.service.user

import dev.usbharu.hideout.core.domain.model.media.Media

data class UpdateUserDto(
    val screenName: String,
    val description: String,
    val avatarMedia: Media?,
    val headerMedia: Media?,
    val autoAcceptFollowRequest: Boolean,
    val autoAcceptFolloweeFollowRequest: Boolean
)
