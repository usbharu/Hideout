package dev.usbharu.hideout.activitypub.service.activity.undo

import dev.usbharu.hideout.core.domain.model.actor.Actor

interface APSendUndoService {
    suspend fun sendUndoFollow(actor: Actor, target: Actor)
    suspend fun sendUndoBlock(actor: Actor, target: Actor)
}
