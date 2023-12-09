package dev.usbharu.hideout.activitypub.service.activity.accept

import dev.usbharu.hideout.core.domain.model.user.User
import org.springframework.stereotype.Service

interface ApSendAcceptService {
    suspend fun sendAccept(user: User, target: User)
}

@Service
class ApSendAcceptServiceImpl : ApSendAcceptService {
    override suspend fun sendAccept(user: User, target: User) {
        TODO("Not yet implemented")
    }

}
