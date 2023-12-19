package dev.usbharu.hideout.core.query

import dev.usbharu.hideout.core.domain.model.instance.Instance

interface InstanceQueryService {
    suspend fun findByUrl(url: String): Instance?
}
