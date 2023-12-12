package dev.usbharu.hideout.core.domain.model.userdetails

data class UserDetail(
    val actorId: Long,
    val password: String,
    val autoAcceptFolloweeFollowRequest: Boolean
)
