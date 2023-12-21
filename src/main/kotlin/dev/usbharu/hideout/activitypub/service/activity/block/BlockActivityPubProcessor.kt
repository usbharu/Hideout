package dev.usbharu.hideout.activitypub.service.activity.block

import dev.usbharu.hideout.activitypub.domain.model.Block
import dev.usbharu.hideout.activitypub.service.common.AbstractActivityPubProcessor
import dev.usbharu.hideout.activitypub.service.common.ActivityPubProcessContext
import dev.usbharu.hideout.activitypub.service.common.ActivityType
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.exception.resource.UserNotFoundException
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.service.relationship.RelationshipService
import org.springframework.stereotype.Service

/**
 * ブロックアクティビティを処理します
 */
@Service
class BlockActivityPubProcessor(
    private val relationshipService: RelationshipService,
    private val actorRepository: ActorRepository,
    transaction: Transaction
) :
    AbstractActivityPubProcessor<Block>(transaction) {
    override suspend fun internalProcess(activity: ActivityPubProcessContext<Block>) {
        val user = actorRepository.findByUrl(activity.activity.actor)
            ?: throw UserNotFoundException.withUrl(activity.activity.actor)
        val target = actorRepository.findByUrl(activity.activity.apObject) ?: throw UserNotFoundException.withUrl(
            activity.activity.apObject
        )
        relationshipService.block(user.id, target.id)
    }

    override fun isSupported(activityType: ActivityType): Boolean = activityType == ActivityType.Block

    override fun type(): Class<Block> = Block::class.java
}
