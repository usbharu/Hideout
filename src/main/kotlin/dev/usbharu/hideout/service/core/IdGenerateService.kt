package dev.usbharu.hideout.service.core

import org.springframework.stereotype.Service

@Service
interface IdGenerateService {
    suspend fun generateId(): Long
}
