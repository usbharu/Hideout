package dev.usbharu.hideout.activitypub.service.activity.undo

import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.domain.model.Undo
import dev.usbharu.hideout.activitypub.interfaces.api.common.ActivityPubResponse
import dev.usbharu.hideout.activitypub.interfaces.api.common.ActivityPubStringResponse
import dev.usbharu.hideout.activitypub.service.objects.user.APUserService
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.query.UserQueryService
import dev.usbharu.hideout.core.service.user.UserService
import io.ktor.http.*
import org.springframework.stereotype.Service

interface APUndoService {
    suspend fun receiveUndo(undo: Undo): ActivityPubResponse
}

@Service
@Suppress("UnsafeCallOnNullableType")
class APUndoServiceImpl(
    private val userService: UserService,
    private val apUserService: APUserService,
    private val userQueryService: UserQueryService,
    private val transaction: Transaction
) : APUndoService {
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
                transaction.transaction {
                    apUserService.fetchPerson(undo.actor!!, follow.`object`)
                    val follower = userQueryService.findByUrl(undo.actor!!)
                    val target = userQueryService.findByUrl(follow.`object`!!)
                    userService.unfollow(target.id, follower.id)
                }
                return ActivityPubStringResponse(HttpStatusCode.OK, "Accept")
            }

            else -> {}
        }
        TODO()
    }
}
