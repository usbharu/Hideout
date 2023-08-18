package dev.usbharu.hideout.service.core

import org.springframework.stereotype.Service

@Service
interface ServerInitialiseService {
    suspend fun init()
}
