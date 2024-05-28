package dev.usbharu.hideout.core.usecase.actor

import org.springframework.stereotype.Service

@Service
interface SetAlsoKnownAsLocalActorApplicationService {
    suspend fun setAlsoKnownAs(actorId: Long, alsoKnownAs: List<Long>)
}