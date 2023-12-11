package dev.usbharu.hideout.activitypub.service.activity.reject

import dev.usbharu.hideout.core.domain.model.user.User

interface ApSendRejectService {
    suspend fun sendRejectFollow(user: User, target: User)
}
