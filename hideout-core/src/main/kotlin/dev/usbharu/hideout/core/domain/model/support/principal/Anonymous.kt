package dev.usbharu.hideout.core.domain.model.support.principal

import dev.usbharu.hideout.core.domain.model.actor.ActorId

data object Anonymous : Principal(ActorId.ghost, null, null)
