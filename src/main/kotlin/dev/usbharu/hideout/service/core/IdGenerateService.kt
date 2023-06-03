package dev.usbharu.hideout.service.core

interface IdGenerateService {
    suspend fun generateId(): Long
}
