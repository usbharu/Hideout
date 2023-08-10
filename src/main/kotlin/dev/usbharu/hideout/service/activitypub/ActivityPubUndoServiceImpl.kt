package dev.usbharu.hideout.service.activitypub

import dev.usbharu.hideout.domain.model.ActivityPubResponse
import dev.usbharu.hideout.domain.model.ActivityPubStringResponse
import dev.usbharu.hideout.domain.model.ap.Follow
import dev.usbharu.hideout.domain.model.ap.Undo
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.service.user.IUserService
import io.ktor.http.*
import org.koin.core.annotation.Single

@Single
@Suppress("UnsafeCallOnNullableType")
class ActivityPubUndoServiceImpl(
    private val userService: IUserService,
    private val activityPubUserService: ActivityPubUserService,
    private val userQueryService: UserQueryService
) : ActivityPubUndoService {
    override suspend fun receiveUndo(undo: Undo): ActivityPubResponse {
        if (undo.actor == null) {
            return ActivityPubStringResponse(HttpStatusCode.BadRequest, "actor is null")
        }

        val type =
            undo.`object`?.type.orEmpty()
                .firstOrNull { it == "Block" || it == "Follow" || it == "Like" || it == "Announce" || it == "Accept" }
                ?: return ActivityPubStringResponse(HttpStatusCode.BadRequest, "unknown type ${undo.`object`?.type}")

        when (type) {
            "Follow" -> {
                val follow = undo.`object` as Follow

                if (follow.`object` == null) {
                    return ActivityPubStringResponse(HttpStatusCode.BadRequest, "object.object is null")
                }

                activityPubUserService.fetchPerson(undo.actor!!, follow.`object`)
                val follower = userQueryService.findByUrl(undo.actor!!)
                val target = userQueryService.findByUrl(follow.`object`!!)
                userService.unfollow(target.id, follower.id)
            }

            else -> {}
        }
        TODO()
    }
}
