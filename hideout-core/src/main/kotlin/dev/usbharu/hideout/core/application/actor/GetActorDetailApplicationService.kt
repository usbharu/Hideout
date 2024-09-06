package dev.usbharu.hideout.core.application.actor

import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.media.MediaRepository
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class GetActorDetailApplicationService(
    private val actorRepository: ActorRepository,
    private val mediaRepository: MediaRepository,
    private val applicationConfig: ApplicationConfig,
    transaction: Transaction
) :
    AbstractApplicationService<GetActorDetail, ActorDetail>(
        transaction,
        logger
    ) {
    override suspend fun internalExecute(command: GetActorDetail, principal: Principal): ActorDetail {
        val actor = if (command.id != null) {
            actorRepository.findById(ActorId(command.id))
                ?: throw IllegalArgumentException("Actor ${command.id} not found.")
        } else if (command.actorName != null) {
            val host = command.actorName.host.ifEmpty {
                applicationConfig.url.host
            }
            actorRepository.findByNameAndDomain(command.actorName.userpart, host)
                ?: throw IllegalArgumentException("Actor ${command.actorName} not found.")
        } else {
            throw IllegalArgumentException("id and actorName are null.")
        }

        val iconUrl = actor.icon?.let { mediaRepository.findById(it)?.url }
        val bannerUrl = actor.banner?.let { mediaRepository.findById(it)?.url }

        return ActorDetail.of(actor, iconUrl, bannerUrl)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GetActorDetailApplicationService::class.java)
    }
}
