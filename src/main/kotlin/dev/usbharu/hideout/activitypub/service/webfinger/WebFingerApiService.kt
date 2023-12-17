package dev.usbharu.hideout.activitypub.service.webfinger

import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.exception.resource.UserNotFoundException
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import org.springframework.stereotype.Service

@Service
interface WebFingerApiService {
    suspend fun findByNameAndDomain(name: String, domain: String): Actor
}

@Service
class WebFingerApiServiceImpl(
    private val transaction: Transaction,
    private val actorRepository: ActorRepository
) :
    WebFingerApiService {
    override suspend fun findByNameAndDomain(name: String, domain: String): Actor {
        return transaction.transaction {
            actorRepository.findByNameAndDomain(name, domain) ?: throw UserNotFoundException.withNameAndDomain(
                name,
                domain
            )
        }
    }
}
