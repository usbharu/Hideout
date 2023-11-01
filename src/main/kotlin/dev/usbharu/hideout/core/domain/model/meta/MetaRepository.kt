package dev.usbharu.hideout.core.domain.model.meta

import org.springframework.stereotype.Repository

@Repository
interface MetaRepository {

    suspend fun save(meta: Meta)

    suspend fun get(): Meta?
}
