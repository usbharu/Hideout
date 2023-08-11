package dev.usbharu.hideout.service.ap

import dev.usbharu.hideout.domain.model.hideout.dto.SendFollowDto

interface APSendFollowService {
    suspend fun sendFollow(sendFollowDto: SendFollowDto)
}
