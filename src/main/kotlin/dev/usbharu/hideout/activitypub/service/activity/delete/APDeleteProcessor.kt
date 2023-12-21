package dev.usbharu.hideout.activitypub.service.activity.delete

import dev.usbharu.hideout.activitypub.domain.exception.IllegalActivityPubObjectException
import dev.usbharu.hideout.activitypub.domain.model.Delete
import dev.usbharu.hideout.activitypub.domain.model.HasId
import dev.usbharu.hideout.activitypub.domain.model.objects.ObjectValue
import dev.usbharu.hideout.activitypub.service.common.AbstractActivityPubProcessor
import dev.usbharu.hideout.activitypub.service.common.ActivityPubProcessContext
import dev.usbharu.hideout.activitypub.service.common.ActivityType
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.service.post.PostService
import dev.usbharu.hideout.core.service.user.UserService
import org.springframework.stereotype.Service

@Service
class APDeleteProcessor(
    transaction: Transaction,
    private val userService: UserService,
    private val postService: PostService,
    private val actorRepository: ActorRepository,
    private val postRepository: PostRepository
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

        val actor = actorRepository.findByUrl(deleteId)
        actor?.let { userService.deleteRemoteActor(it.id) }

        val post = postRepository.findByApId(deleteId)
        if (post == null) {
            logger.warn("FAILED Delete id: {} is not found.", deleteId)
            return
        }
        postService.deleteRemote(post)
    }

    override fun isSupported(activityType: ActivityType): Boolean = activityType == ActivityType.Delete

    override fun type(): Class<Delete> = Delete::class.java
}
