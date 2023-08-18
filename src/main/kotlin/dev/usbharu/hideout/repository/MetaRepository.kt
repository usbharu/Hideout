package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.Meta
import org.springframework.stereotype.Repository

@Repository
interface MetaRepository {

    suspend fun save(meta: Meta)

    suspend fun get(): Meta?
}
