package dev.usbharu.hideout.application.service.id

import org.springframework.stereotype.Service

@Service
interface IdGenerateService {
    suspend fun generateId(): Long
}
