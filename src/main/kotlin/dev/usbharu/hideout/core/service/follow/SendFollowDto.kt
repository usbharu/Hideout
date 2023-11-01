package dev.usbharu.hideout.core.service.follow

import dev.usbharu.hideout.core.domain.model.user.User

data class SendFollowDto(val userId: User, val followTargetUserId: User)
