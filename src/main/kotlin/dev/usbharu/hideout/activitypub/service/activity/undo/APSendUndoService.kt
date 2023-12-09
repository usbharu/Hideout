package dev.usbharu.hideout.activitypub.service.activity.undo

import dev.usbharu.hideout.core.domain.model.user.User

interface APSendUndoService {
    suspend fun sendUndoFollow(user: User, target: User)
    suspend fun sendUndoBlock(user: User, target: User)
}
