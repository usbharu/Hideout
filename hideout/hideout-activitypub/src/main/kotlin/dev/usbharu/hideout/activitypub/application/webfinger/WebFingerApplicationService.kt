package dev.usbharu.hideout.activitypub.application.webfinger

import dev.usbharu.hideout.activitypub.interfaces.wellknown.Link
import dev.usbharu.hideout.activitypub.interfaces.wellknown.XRD
import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.support.domain.apHost
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.URI

@Service
class WebFingerApplicationService(
    transaction: Transaction,
    private val applicationConfig: ApplicationConfig,
    private val actorRepository: ActorRepository,
) : AbstractApplicationService<String, XRD>(transaction, logger) {

    override suspend fun internalExecute(resource: String, principal: Principal): XRD {
        if (resource.startsWith("acct:").not()) {
            throw IllegalArgumentException("Parameter (resource) is invalid.")
        }
        val acct = resource.substringAfter("acct:")

        val host = acct.substringAfter('@', "")
        if (applicationConfig.url.apHost != host) {
            throw IllegalArgumentException("Parameter (resource) is invalid.")
        }
        val username = acct.substringBefore('@', "")
        if (username.isEmpty()) {
            throw IllegalArgumentException("Actor is not found.")
        }

        val actor = actorRepository.findByNameAndDomain(username, applicationConfig.url.apHost)
            ?: throw IllegalArgumentException("Actor $username not found.")

        return XRD(
            listOf(Link("self", null, "application/activity+json", actor.url.toString())),
            URI.create("acct:${actor.name.name}@${actor.domain.domain}")
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WebFingerApplicationService::class.java)
    }
}
