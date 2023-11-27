package dev.usbharu.hideout.activitypub.service.activity.delete

import dev.usbharu.hideout.activitypub.domain.exception.IllegalActivityPubObjectException
import dev.usbharu.hideout.activitypub.domain.model.Delete
import dev.usbharu.hideout.activitypub.service.common.AbstractActivityPubProcessor
import dev.usbharu.hideout.activitypub.service.common.ActivityPubProcessContext
import dev.usbharu.hideout.activitypub.service.common.ActivityType
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.query.PostQueryService

class APDeleteProcessor(
    transaction: Transaction,
    private val postQueryService: PostQueryService,
    private val postRepository: PostRepository
) :
    AbstractActivityPubProcessor<Delete>(transaction) {
    override suspend fun internalProcess(activity: ActivityPubProcessContext<Delete>) {
        val deleteId = activity.activity.`object`?.id ?: throw IllegalActivityPubObjectException("object.id is null")

        val post = try {
            postQueryService.findByApId(deleteId)
        } catch (e: FailedToGetResourcesException) {
            logger.warn("FAILED delete id: {} is not found.", deleteId, e)
            return
        }

        postRepository.delete(post.id)
    }

    override fun isSupported(activityType: ActivityType): Boolean = activityType == ActivityType.Delete

    override fun type(): Class<Delete> = Delete::class.java
}
