package dev.usbharu.hideout.core.service.block

import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.domain.model.Reject
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.domain.model.block.Block
import dev.usbharu.hideout.core.domain.model.block.BlockRepository
import dev.usbharu.hideout.core.domain.model.user.UserRepository
import dev.usbharu.hideout.core.external.job.DeliverBlockJob
import dev.usbharu.hideout.core.external.job.DeliverBlockJobParam
import dev.usbharu.hideout.core.query.FollowerQueryService
import dev.usbharu.hideout.core.service.job.JobQueueParentService
import dev.usbharu.hideout.core.service.user.UserService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class BlockServiceImpl(
    private val transaction: Transaction,
    private val blockRepository: BlockRepository,
    private val followerQueryService: FollowerQueryService,
    private val userService: UserService,
    private val jobQueueParentService: JobQueueParentService,
    private val deliverBlockJob: DeliverBlockJob,
    private val userRepository: UserRepository,
    private val applicationConfig: ApplicationConfig
) :
    BlockService {
    override suspend fun block(userId: Long, target: Long) {
        logger.debug("Block userId: {} → target: {}", userId, target)
        blockRepository.save(Block(userId, target))
        if (followerQueryService.alreadyFollow(userId, target)) {
            logger.debug("Unfollow (Block) userId: {} → target: {}", userId, target)
            userService.unfollow(userId, target)
        }

        val user = userRepository.findById(userId) ?: throw IllegalStateException("Block user was not found.")

        if (user.domain == applicationConfig.url.host) {
            return
        }

        val target = userRepository.findById(target) ?: throw IllegalStateException("Block use was not found.")

        if (target.domain == applicationConfig.url.host) {
            return
        }

        val blockJobParam = DeliverBlockJobParam(
            user.id,
            dev.usbharu.hideout.activitypub.domain.model.Block(
                user.url,
                "${applicationConfig.url}/block/${user.id}/${target.id}",
                target.url
            ),
            Reject(
                user.url,
                "${applicationConfig.url}/reject/${user.id}/${target.id}",
                Follow(
                    apObject = user.url,
                    actor = target.url
                )
            ),
            target.inbox
        )
        jobQueueParentService.scheduleTypeSafe(deliverBlockJob, blockJobParam)
    }

    override suspend fun unblock(userId: Long, target: Long) = transaction.transaction {
        logger.debug("Unblock userId: {} → target: {}", userId, target)
        try {
            val block = blockRepository.findByUserIdAndTarget(userId, target)
            blockRepository.delete(block)
        } catch (e: FailedToGetResourcesException) {
            logger.warn("FAILED Unblock userId: {} target: {}", userId, target, e)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BlockServiceImpl::class.java)
    }
}
