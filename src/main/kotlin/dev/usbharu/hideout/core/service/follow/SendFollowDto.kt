package dev.usbharu.hideout.core.service.follow

import dev.usbharu.hideout.core.domain.model.actor.Actor

data class SendFollowDto(val actorId: Actor, val followTargetActorId: Actor)
