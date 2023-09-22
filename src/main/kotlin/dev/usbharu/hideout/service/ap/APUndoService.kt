package dev.usbharu.hideout.service.ap

import dev.usbharu.hideout.domain.model.ActivityPubResponse
import dev.usbharu.hideout.domain.model.ActivityPubStringResponse
import dev.usbharu.hideout.domain.model.ap.Follow
import dev.usbharu.hideout.domain.model.ap.Undo
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.service.core.Transaction
import dev.usbharu.hideout.service.user.UserService
import io.ktor.http.*
import org.springframework.stereotype.Service

@Service
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
            }

            else -> {}
        }
        TODO()
    }
}
