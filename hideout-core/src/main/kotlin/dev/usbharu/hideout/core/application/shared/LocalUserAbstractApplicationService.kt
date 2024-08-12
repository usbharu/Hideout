package dev.usbharu.hideout.core.application.shared

import dev.usbharu.hideout.core.application.exception.PermissionDeniedException
import dev.usbharu.hideout.core.domain.model.support.principal.LocalUser
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import org.slf4j.Logger

abstract class LocalUserAbstractApplicationService<T : Any, R>(transaction: Transaction, logger: Logger) :
    AbstractApplicationService<T, R>(transaction, logger) {
    override suspend fun internalExecute(command: T, principal: Principal): R {
        if (principal !is LocalUser) {
            throw PermissionDeniedException()
        }
        return internalExecute(command, principal)
    }

    protected abstract suspend fun internalExecute(command: T, principal: LocalUser): R
}