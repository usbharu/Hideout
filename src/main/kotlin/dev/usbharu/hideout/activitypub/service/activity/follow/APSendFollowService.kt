package dev.usbharu.hideout.activitypub.service.activity.follow

import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.service.common.APRequestService
import dev.usbharu.hideout.core.service.follow.SendFollowDto
import org.springframework.stereotype.Service

interface APSendFollowService {
    suspend fun sendFollow(sendFollowDto: SendFollowDto)
}

@Service
class APSendFollowServiceImpl(
    private val apRequestService: APRequestService,
) : APSendFollowService {
    override suspend fun sendFollow(sendFollowDto: SendFollowDto) {
        val follow = Follow(
            apObject = sendFollowDto.followTargetActorId.url,
            actor = sendFollowDto.actorId.url
        )

        apRequestService.apPost(sendFollowDto.followTargetActorId.inbox, follow, sendFollowDto.actorId)
    }
}
