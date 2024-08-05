package dev.usbharu.hideout.core.application.shared

import dev.usbharu.hideout.core.domain.model.support.principal.FromApi
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import org.slf4j.Logger

abstract class LocalUserAbstractApplicationService<T : Any, R>(transaction: Transaction, logger: Logger) :
    AbstractApplicationService<T, R>(transaction, logger) {
    override suspend fun internalExecute(command: T, principal: Principal): R {
        require(principal is FromApi)
        return internalExecute(command, principal)
    }

    abstract suspend fun internalExecute(command: T, principal: FromApi): R
}