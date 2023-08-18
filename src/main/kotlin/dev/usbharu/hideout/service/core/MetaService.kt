package dev.usbharu.hideout.service.core

import dev.usbharu.hideout.domain.model.hideout.entity.Jwt
import dev.usbharu.hideout.domain.model.hideout.entity.Meta
import org.springframework.stereotype.Service

@Service
interface MetaService {
    suspend fun getMeta(): Meta
    suspend fun updateMeta(meta: Meta)
    suspend fun getJwtMeta(): Jwt
}