package dev.usbharu.hideout.core.domain.model.support.principal

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.support.acct.Acct
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId

sealed class Principal(open val actorId: ActorId, open val userDetailId: UserDetailId?, open val acct: Acct?)