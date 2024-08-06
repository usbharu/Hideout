package dev.usbharu.hideout.core.query.principal

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId

data class PrincipalDTO(val userDetailId: UserDetailId, val actorId: ActorId, val username: String, val host: String)