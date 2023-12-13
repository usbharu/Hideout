package dev.usbharu.hideout.activitypub.service.activity.delete

import dev.usbharu.hideout.activitypub.domain.exception.IllegalActivityPubObjectException
import dev.usbharu.hideout.activitypub.domain.model.Delete
import dev.usbharu.hideout.activitypub.domain.model.HasId
import dev.usbharu.hideout.activitypub.domain.model.objects.ObjectValue
import dev.usbharu.hideout.activitypub.service.common.AbstractActivityPubProcessor
import dev.usbharu.hideout.activitypub.service.common.ActivityPubProcessContext
import dev.usbharu.hideout.activitypub.service.common.ActivityType
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.query.ActorQueryService
import dev.usbharu.hideout.core.query.PostQueryService
import dev.usbharu.hideout.core.service.post.PostService
import dev.usbharu.hideout.core.service.user.UserService
import org.springframework.stereotype.Service

@Service
class APDeleteProcessor(
    transaction: Transaction,
    private val postQueryService: PostQueryService,
    private val actorQueryService: ActorQueryService,
    private val userService: UserService,
    private val postService: PostService
) :
    AbstractActivityPubProcessor<Delete>(transaction) {
    override suspend fun internalProcess(activity: ActivityPubProcessContext<Delete>) {
        val value = activity.activity.apObject
        val deleteId = if (value is HasId) {
            value.id
        } else if (value is ObjectValue) {
            value.`object`
        } else {
            throw IllegalActivityPubObjectException("object hasn't id or object")
        }

        try {
            val actor = actorQueryService.findByUrl(deleteId)
            userService.deleteRemoteActor(actor.id)
        } catch (e: Exception) {
            logger.warn("FAILED delete id: {} is not found.", deleteId, e)
        }

        try {
            val post = postQueryService.findByApId(deleteId)
            postService.deleteRemote(post)
        } catch (e: FailedToGetResourcesException) {
            logger.warn("FAILED delete id: {} is not found.", deleteId, e)
        }


    }

    override fun isSupported(activityType: ActivityType): Boolean = activityType == ActivityType.Delete

    override fun type(): Class<Delete> = Delete::class.java
}
