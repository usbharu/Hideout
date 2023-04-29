package dev.usbharu.hideout.service

interface IdGenerateService {
    suspend fun generateId(): Long
}
