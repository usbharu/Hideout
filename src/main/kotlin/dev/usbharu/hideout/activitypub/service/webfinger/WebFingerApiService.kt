package dev.usbharu.hideout.activitypub.service.webfinger

import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.query.ActorQueryService
import org.springframework.stereotype.Service

@Service
interface WebFingerApiService {
    suspend fun findByNameAndDomain(name: String, domain: String): Actor
}

@Service
class WebFingerApiServiceImpl(private val transaction: Transaction, private val actorQueryService: ActorQueryService) :
    WebFingerApiService {
    override suspend fun findByNameAndDomain(name: String, domain: String): Actor {
        return transaction.transaction {
            actorQueryService.findByNameAndDomain(name, domain)
        }
    }
}
