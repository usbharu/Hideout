package dev.usbharu.hideout.activitypub.service.activity.accept

import dev.usbharu.hideout.activitypub.domain.exception.IllegalActivityPubObjectException
import dev.usbharu.hideout.activitypub.domain.model.Accept
import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.interfaces.api.common.ActivityPubResponse
import dev.usbharu.hideout.activitypub.interfaces.api.common.ActivityPubStringResponse
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.query.FollowerQueryService
import dev.usbharu.hideout.core.query.UserQueryService
import dev.usbharu.hideout.core.service.user.UserService
import io.ktor.http.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

interface APAcceptService {
    suspend fun receiveAccept(accept: Accept): ActivityPubResponse
}

@Service
class APAcceptServiceImpl(
    private val userService: UserService,
    private val userQueryService: UserQueryService,
    private val followerQueryService: FollowerQueryService,
    private val transaction: Transaction
) : APAcceptService {
    override suspend fun receiveAccept(accept: Accept): ActivityPubResponse {
        return transaction.transaction {
            LOGGER.debug("START Follow")
            LOGGER.trace("{}", accept)
            val value = accept.`object` ?: throw IllegalActivityPubObjectException("object is null")
            if (value.type.contains("Follow").not()) {
                LOGGER.warn("FAILED Activity type is not 'Follow'")
                throw IllegalActivityPubObjectException("Invalid type ${value.type}")
            }

            val follow = value as Follow
            val userUrl = follow.`object` ?: throw IllegalActivityPubObjectException("object is null")
            val followerUrl = follow.actor ?: throw IllegalActivityPubObjectException("actor is null")

            val user = userQueryService.findByUrl(userUrl)
            val follower = userQueryService.findByUrl(followerUrl)

            if (followerQueryService.alreadyFollow(user.id, follower.id)) {
                LOGGER.debug("END User already follow from ${follower.url} to ${user.url}")
                return@transaction ActivityPubStringResponse(HttpStatusCode.OK, "accepted")
            }
            userService.follow(user.id, follower.id)
            LOGGER.debug("SUCCESS Follow from ${follower.url} to ${user.url}.")
            ActivityPubStringResponse(HttpStatusCode.OK, "accepted")
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(APAcceptServiceImpl::class.java)
    }
}
