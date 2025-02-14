package dev.usbharu.hideout.core.domain.model.support.principal

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.support.acct.Acct

class RemoteUser(actorId: ActorId, acct: Acct?) : Principal(actorId, null, acct)
