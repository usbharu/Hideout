package dev.usbharu.hideout.service.ap

import com.fasterxml.jackson.databind.ObjectMapper
import dev.usbharu.hideout.domain.model.ap.Follow
import dev.usbharu.hideout.domain.model.hideout.dto.SendFollowDto
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

interface APSendFollowService {
    suspend fun sendFollow(sendFollowDto: SendFollowDto)
}

@Service
class APSendFollowServiceImpl(
    @Qualifier("activitypub") private val objectMapper: ObjectMapper,
    private val apRequestService: APRequestService,
) : APSendFollowService {
    override suspend fun sendFollow(sendFollowDto: SendFollowDto) {
        val follow = Follow(
            name = "Follow",
            `object` = sendFollowDto.followTargetUserId.url,
            actor = sendFollowDto.userId.url
        )

        apRequestService.apPost(sendFollowDto.followTargetUserId.inbox, follow, sendFollowDto.userId)
    }
}
