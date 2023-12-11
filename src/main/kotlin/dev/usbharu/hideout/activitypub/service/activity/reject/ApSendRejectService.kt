package dev.usbharu.hideout.activitypub.service.activity.reject

import dev.usbharu.hideout.core.domain.model.actor.Actor

interface ApSendRejectService {
    suspend fun sendRejectFollow(actor: Actor, target: Actor)
}
