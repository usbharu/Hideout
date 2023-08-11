package dev.usbharu.hideout.service.ap

import dev.usbharu.hideout.domain.model.ap.Follow
import dev.usbharu.hideout.domain.model.hideout.dto.SendFollowDto
import dev.usbharu.hideout.plugins.postAp
import io.ktor.client.*
import org.koin.core.annotation.Single

@Single
class APSendFollowServiceImpl(private val httpClient: HttpClient) : APSendFollowService {
    override suspend fun sendFollow(sendFollowDto: SendFollowDto) {
        val follow = Follow(
            name = "Follow",
            `object` = sendFollowDto.followTargetUserId.url,
            actor = sendFollowDto.userId.url
        )
        httpClient.postAp(
            urlString = sendFollowDto.followTargetUserId.inbox,
            username = sendFollowDto.userId.url,
            jsonLd = follow
        )
    }
}
