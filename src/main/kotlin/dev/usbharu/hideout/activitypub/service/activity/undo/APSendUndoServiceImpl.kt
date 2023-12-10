package dev.usbharu.hideout.activitypub.service.activity.undo

import dev.usbharu.hideout.core.domain.model.user.User
import org.springframework.stereotype.Service

@Service
class APSendUndoServiceImpl : APSendUndoService {
    override suspend fun sendUndoFollow(user: User, target: User) {
        TODO("Not yet implemented")
    }

    override suspend fun sendUndoBlock(user: User, target: User) {
        TODO("Not yet implemented")
    }
}
