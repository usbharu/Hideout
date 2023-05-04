package dev.usbharu.hideout.service

import dev.usbharu.hideout.domain.model.hideout.entity.Jwt
import dev.usbharu.hideout.domain.model.hideout.entity.Meta
import dev.usbharu.hideout.exception.NotInitException
import dev.usbharu.hideout.repository.IMetaRepository
import org.koin.core.annotation.Single

@Single
class MetaServiceImpl(private val metaRepository: IMetaRepository) : IMetaService {
    override suspend fun getMeta(): Meta = metaRepository.get() ?: throw NotInitException("Meta is null")

    override suspend fun updateMeta(meta: Meta) {
        metaRepository.save(meta)
    }

    override suspend fun getJwtMeta(): Jwt = getMeta().jwt
}
