package dev.usbharu.hideout.core.application.instance

import dev.usbharu.hideout.core.application.exception.InternalServerException
import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.instance.InstanceRepository
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class GetLocalInstanceApplicationService(
    private val applicationConfig: ApplicationConfig,
    private val instanceRepository: InstanceRepository,
    transaction: Transaction
) :
    AbstractApplicationService<Unit, Instance>(
        transaction,
        logger
    ) {
    var cachedInstance: Instance? = null

    override suspend fun internalExecute(command: Unit, principal: Principal): Instance {
        if (cachedInstance != null) {
            return cachedInstance!!
        }

        val instance = (
            instanceRepository.findByUrl(applicationConfig.url.toURI())
                ?: throw InternalServerException("Local instance not found.")
            )

        cachedInstance = Instance.of(instance)
        return cachedInstance!!
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GetLocalInstanceApplicationService::class.java)
    }
}
