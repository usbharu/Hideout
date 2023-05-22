package dev.usbharu.hideout.domain.model.hideout.dto

import dev.usbharu.hideout.domain.model.hideout.entity.User

data class SendFollowDto(val userId: User, val followTargetUserId: User)
