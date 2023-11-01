package dev.usbharu.hideout.application.service.init

import org.springframework.stereotype.Service

@Service
interface ServerInitialiseService {
    suspend fun init()
}
