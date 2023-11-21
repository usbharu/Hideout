package dev.usbharu.hideout.activitypub.service.activity.undo

import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.domain.model.Undo
import dev.usbharu.hideout.activitypub.service.objects.user.APUserService
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.query.UserQueryService
import dev.usbharu.hideout.core.service.user.UserService
import org.springframework.stereotype.Service

interface APUndoService {
    suspend fun receiveUndo(undo: Undo)
}

@Service
@Suppress("UnsafeCallOnNullableType")
class APUndoServiceImpl(
    private val userService: UserService,
    private val apUserService: APUserService,
    private val userQueryService: UserQueryService,
    private val transaction: Transaction
) : APUndoService {
    override suspend fun receiveUndo(undo: Undo) {
        if (undo.actor == null) {
            return
        }

        val type =
            undo.`object`?.type.orEmpty()
                .firstOrNull { it == "Block" || it == "Follow" || it == "Like" || it == "Announce" || it == "Accept" }
                ?: return

        when (type) {
            "Follow" -> {
                val follow = undo.`object` as Follow

                if (follow.`object` == null) {
                    return
                }
                transaction.transaction {
                    apUserService.fetchPerson(undo.actor!!, follow.`object`)
                    val follower = userQueryService.findByUrl(undo.actor!!)
                    val target = userQueryService.findByUrl(follow.`object`!!)
                    userService.unfollow(target.id, follower.id)
                }
                return
            }

            else -> {}
        }
        TODO()
    }
}
