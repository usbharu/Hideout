package dev.usbharu.hideout.core.application.actor

import dev.usbharu.hideout.core.domain.model.support.acct.Acct

data class GetActorDetail(
    val actorName: Acct? = null,
    val id: Long? = null
)
