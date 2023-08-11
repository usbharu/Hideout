package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.Meta

interface MetaRepository {

    suspend fun save(meta: Meta)

    suspend fun get(): Meta?
}
