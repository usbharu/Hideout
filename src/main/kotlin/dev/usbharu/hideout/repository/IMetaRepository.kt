package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.Meta

interface IMetaRepository {

    suspend fun save(meta: Meta)

    suspend fun get(): Meta?
}
