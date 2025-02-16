package dev.usbharu.hideout.activitypub.application.actor

import dev.usbharu.activitystreamsserialization.other.JsonLd
import dev.usbharu.hideout.activitypub.external.activitystreams.ActorTranslator
import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.support.domain.apHost
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class GetActorApplicationService(
    private val actorRepository: ActorRepository,
    private val applicationConfig: ApplicationConfig,
    private val actorTranslator: ActorTranslator,
    transaction: Transaction,
) : AbstractApplicationService<String, JsonLd>(
    transaction,
    logger,
) {
    override suspend fun internalExecute(command: String, principal: Principal): JsonLd {
        val actor = actorRepository.findByNameAndDomain(command, applicationConfig.url.apHost)
            ?: throw IllegalArgumentException("Actor $command not found")
        return actorTranslator.translate(actor, null, null)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GetActorApplicationService::class.java)
    }
}
