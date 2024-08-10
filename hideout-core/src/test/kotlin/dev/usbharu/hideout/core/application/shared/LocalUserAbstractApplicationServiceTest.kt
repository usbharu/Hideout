package dev.usbharu.hideout.core.application.shared

import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import dev.usbharu.hideout.core.domain.model.support.principal.FromApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import utils.TestTransaction

class LocalUserAbstractApplicationServiceTest {
    @Test
    fun requireFromAPI() = runTest {
        val logger = LoggerFactory.getLogger(javaClass)
        val value = object : LocalUserAbstractApplicationService<Unit, Unit>(TestTransaction, logger) {
            override suspend fun internalExecute(command: Unit, principal: FromApi) {

            }
        }

        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            value.execute(Unit, Anonymous)
        }
    }
}