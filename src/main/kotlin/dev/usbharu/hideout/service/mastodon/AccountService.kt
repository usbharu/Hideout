package dev.usbharu.hideout.service.mastodon

import org.springframework.stereotype.Service

@Service
interface AccountService {
    suspend fun findById()
}
