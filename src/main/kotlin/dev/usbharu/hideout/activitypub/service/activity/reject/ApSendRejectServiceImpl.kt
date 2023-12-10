package dev.usbharu.hideout.activitypub.service.activity.reject

import dev.usbharu.hideout.core.domain.model.user.User
import org.springframework.stereotype.Service

@Service
class ApSendRejectServiceImpl : ApSendRejectService {
    override suspend fun sendReject(user: User, target: User) {
        TODO("Not yet implemented")
    }
}
