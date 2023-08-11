package dev.usbharu.hideout.service.api

import dev.usbharu.hideout.domain.model.hideout.entity.User

interface WebFingerApiService {
    suspend fun findByNameAndDomain(name: String, domain: String): User
}
