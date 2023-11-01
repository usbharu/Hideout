package dev.usbharu.hideout.application.service.init

import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.exception.NotInitException
import dev.usbharu.hideout.core.domain.model.meta.Jwt
import dev.usbharu.hideout.core.domain.model.meta.Meta
import dev.usbharu.hideout.core.domain.model.meta.MetaRepository
import org.springframework.stereotype.Service

@Service
class MetaServiceImpl(private val metaRepository: MetaRepository, private val transaction: Transaction) :
    MetaService {
    override suspend fun getMeta(): Meta =
        transaction.transaction { metaRepository.get() ?: throw NotInitException("Meta is null") }

    override suspend fun updateMeta(meta: Meta): Unit = transaction.transaction {
        metaRepository.save(meta)
    }

    override suspend fun getJwtMeta(): Jwt = getMeta().jwt
}
