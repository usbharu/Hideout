package dev.usbharu.hideout.application.service.init

import dev.usbharu.hideout.core.domain.model.meta.Jwt
import dev.usbharu.hideout.core.domain.model.meta.Meta
import org.springframework.stereotype.Service

@Service
interface MetaService {
    suspend fun getMeta(): Meta
    suspend fun updateMeta(meta: Meta)
    suspend fun getJwtMeta(): Jwt
}
