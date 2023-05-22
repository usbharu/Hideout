package dev.usbharu.hideout.service.activitypub

import dev.usbharu.hideout.domain.model.ActivityPubResponse
import dev.usbharu.hideout.domain.model.ActivityPubStringResponse
import dev.usbharu.hideout.domain.model.ap.Accept
import dev.usbharu.hideout.domain.model.ap.Follow
import dev.usbharu.hideout.exception.ap.IllegalActivityPubObjectException
import dev.usbharu.hideout.service.impl.IUserService
import io.ktor.http.*
import org.koin.core.annotation.Single

@Single
class ActivityPubAcceptServiceImpl(private val userService: IUserService) : ActivityPubAcceptService {
    override suspend fun receiveAccept(accept: Accept): ActivityPubResponse {
        val value = accept.`object` ?: throw IllegalActivityPubObjectException("object is null")
        if (value.type.contains("Follow").not()) {
            throw IllegalActivityPubObjectException("Invalid type ${value.type}")
        }

        val follow = value as Follow
        val userUrl = follow.`object` ?: throw IllegalActivityPubObjectException("object is null")
        val followerUrl = follow.actor ?: throw IllegalActivityPubObjectException("actor is null")
        val user = userService.findByUrl(userUrl)
        val follower = userService.findByUrl(followerUrl)
        userService.follow(user.id, follower.id)
        return ActivityPubStringResponse(HttpStatusCode.OK, "accepted")
    }
}
