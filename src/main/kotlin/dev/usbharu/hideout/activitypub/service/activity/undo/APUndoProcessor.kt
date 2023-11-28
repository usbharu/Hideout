package dev.usbharu.hideout.activitypub.service.activity.undo

import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.domain.model.Undo
import dev.usbharu.hideout.activitypub.service.common.AbstractActivityPubProcessor
import dev.usbharu.hideout.activitypub.service.common.ActivityPubProcessContext
import dev.usbharu.hideout.activitypub.service.common.ActivityType
import dev.usbharu.hideout.activitypub.service.objects.user.APUserService
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.query.UserQueryService
import dev.usbharu.hideout.core.service.user.UserService

class APUndoProcessor(
    transaction: Transaction,
    private val apUserService: APUserService,
    private val userQueryService: UserQueryService,
    private val userService: UserService
) :
    AbstractActivityPubProcessor<Undo>(transaction) {
    override suspend fun internalProcess(activity: ActivityPubProcessContext<Undo>) {
        val undo = activity.activity
        if (undo.actor == null) {
            return
        }

        val type =
            undo.`object`.type.orEmpty()
                .firstOrNull { it == "Block" || it == "Follow" || it == "Like" || it == "Announce" || it == "Accept" }
                ?: return

        when (type) {
            "Follow" -> {
                val follow = undo.`object` as Follow

                if (follow.apObject == null) {
                    return
                }
                apUserService.fetchPerson(undo.actor, follow.apObject)
                val follower = userQueryService.findByUrl(undo.actor)
                val target = userQueryService.findByUrl(follow.apObject)
                userService.unfollow(target.id, follower.id)
                return
            }

            else -> {}
        }
        TODO()
    }

    override fun isSupported(activityType: ActivityType): Boolean = activityType == ActivityType.Undo

    override fun type(): Class<Undo> = Undo::class.java
}
