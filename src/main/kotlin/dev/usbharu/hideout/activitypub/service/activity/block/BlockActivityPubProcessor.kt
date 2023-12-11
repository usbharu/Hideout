package dev.usbharu.hideout.activitypub.service.activity.block

import dev.usbharu.hideout.activitypub.domain.model.Block
import dev.usbharu.hideout.activitypub.service.common.AbstractActivityPubProcessor
import dev.usbharu.hideout.activitypub.service.common.ActivityPubProcessContext
import dev.usbharu.hideout.activitypub.service.common.ActivityType
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.query.UserQueryService
import dev.usbharu.hideout.core.service.relationship.RelationshipService
import org.springframework.stereotype.Service

/**
 * ブロックアクティビティを処理します
 */
@Service
class BlockActivityPubProcessor(
    private val userQueryService: UserQueryService,
    private val relationshipService: RelationshipService,
    transaction: Transaction
) :
    AbstractActivityPubProcessor<Block>(transaction) {
    override suspend fun internalProcess(activity: ActivityPubProcessContext<Block>) {
        val user = userQueryService.findByUrl(activity.activity.actor)
        val target = userQueryService.findByUrl(activity.activity.apObject)
        relationshipService.block(user.id, target.id)
    }

    override fun isSupported(activityType: ActivityType): Boolean = activityType == ActivityType.Block

    override fun type(): Class<Block> = Block::class.java
}
