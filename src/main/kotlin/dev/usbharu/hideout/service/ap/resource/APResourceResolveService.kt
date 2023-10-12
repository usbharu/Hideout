package dev.usbharu.hideout.service.ap.resource

import dev.usbharu.hideout.domain.model.ap.Object
import dev.usbharu.hideout.domain.model.hideout.entity.User

interface APResourceResolveService {
    suspend fun resolve(url: String, singerId: Long?): Object
    suspend fun resolve(url: String, singer: User?): Object
}
