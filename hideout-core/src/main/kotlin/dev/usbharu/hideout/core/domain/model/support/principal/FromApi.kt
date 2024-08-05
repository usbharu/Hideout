package dev.usbharu.hideout.core.domain.model.support.principal

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.support.acct.Acct
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId

class FromApi(actorId: ActorId, override val userDetailId: UserDetailId, override val acct: Acct) : Principal(
    actorId, userDetailId,
    acct
)