package dev.usbharu.hideout.service.core

import dev.usbharu.hideout.domain.model.hideout.entity.Jwt
import dev.usbharu.hideout.domain.model.hideout.entity.Meta
import dev.usbharu.hideout.exception.NotInitException
import dev.usbharu.hideout.repository.MetaRepository
import org.springframework.stereotype.Service

@Service
class MetaServiceImpl(private val metaRepository: MetaRepository, private val transaction: Transaction) :
    MetaService {
    override suspend fun getMeta(): Meta =
        transaction.transaction { metaRepository.get() ?: throw NotInitException("Meta is null") }

    override suspend fun updateMeta(meta: Meta) = transaction.transaction {
        metaRepository.save(meta)
    }

    override suspend fun getJwtMeta(): Jwt = getMeta().jwt
}
