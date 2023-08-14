package dev.usbharu.hideout.service.ap

import dev.usbharu.hideout.domain.model.ActivityPubResponse
import dev.usbharu.hideout.domain.model.ActivityPubStringResponse
import dev.usbharu.hideout.domain.model.ap.Accept
import dev.usbharu.hideout.domain.model.ap.Follow
import dev.usbharu.hideout.exception.ap.IllegalActivityPubObjectException
import dev.usbharu.hideout.query.FollowerQueryService
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.service.core.Transaction
import dev.usbharu.hideout.service.user.UserService
import io.ktor.http.*
import org.koin.core.annotation.Single

interface APAcceptService {
    suspend fun receiveAccept(accept: Accept): ActivityPubResponse
}

@Single
class APAcceptServiceImpl(
    private val userService: UserService,
    private val userQueryService: UserQueryService,
    private val followerQueryService: FollowerQueryService,
    private val transaction: Transaction
) : APAcceptService {
    override suspend fun receiveAccept(accept: Accept): ActivityPubResponse {
        return transaction.transaction {
            val value = accept.`object` ?: throw IllegalActivityPubObjectException("object is null")
            if (value.type.contains("Follow").not()) {
                throw IllegalActivityPubObjectException("Invalid type ${value.type}")
            }

            val follow = value as Follow
            val userUrl = follow.`object` ?: throw IllegalActivityPubObjectException("object is null")
            val followerUrl = follow.actor ?: throw IllegalActivityPubObjectException("actor is null")
            val user = userQueryService.findByUrl(userUrl)
            val follower = userQueryService.findByUrl(followerUrl)
            if (followerQueryService.alreadyFollow(user.id, follower.id)) {
                return@transaction ActivityPubStringResponse(HttpStatusCode.OK, "accepted")
            }
            userService.follow(user.id, follower.id)
            ActivityPubStringResponse(HttpStatusCode.OK, "accepted")
        }
    }
}
