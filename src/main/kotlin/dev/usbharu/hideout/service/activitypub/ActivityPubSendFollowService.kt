package dev.usbharu.hideout.service.activitypub

import dev.usbharu.hideout.domain.model.hideout.dto.SendFollowDto

interface ActivityPubSendFollowService {
    suspend fun sendFollow(sendFollowDto: SendFollowDto)
}
