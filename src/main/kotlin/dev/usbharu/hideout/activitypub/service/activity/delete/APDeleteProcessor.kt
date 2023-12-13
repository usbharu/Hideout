package dev.usbharu.hideout.activitypub.service.activity.delete

import dev.usbharu.hideout.activitypub.domain.exception.IllegalActivityPubObjectException
import dev.usbharu.hideout.activitypub.domain.model.Delete
import dev.usbharu.hideout.activitypub.domain.model.HasId
import dev.usbharu.hideout.activitypub.service.common.AbstractActivityPubProcessor
import dev.usbharu.hideout.activitypub.service.common.ActivityPubProcessContext
import dev.usbharu.hideout.activitypub.service.common.ActivityType
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.query.PostQueryService
import dev.usbharu.hideout.core.service.post.PostService
import org.springframework.stereotype.Service

@Service
class APDeleteProcessor(
    transaction: Transaction,
    private val postQueryService: PostQueryService,
    private val postService: PostService
) :
    AbstractActivityPubProcessor<Delete>(transaction) {
    override suspend fun internalProcess(activity: ActivityPubProcessContext<Delete>) {
        val value = activity.activity.apObject
        if (value !is HasId) {
            throw IllegalActivityPubObjectException("object hasn't id")
        }
        val deleteId = value.id

        val post = try {
            postQueryService.findByApId(deleteId)
        } catch (e: FailedToGetResourcesException) {
            logger.warn("FAILED delete id: {} is not found.", deleteId, e)
            return
        }

        postService.deleteRemote(post)
    }

    override fun isSupported(activityType: ActivityType): Boolean = activityType == ActivityType.Delete

    override fun type(): Class<Delete> = Delete::class.java
}
