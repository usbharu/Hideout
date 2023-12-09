package dev.usbharu.hideout.core.service.relationship

import dev.usbharu.hideout.activitypub.service.activity.accept.ApSendAcceptService
import dev.usbharu.hideout.activitypub.service.activity.block.APSendBlockService
import dev.usbharu.hideout.activitypub.service.activity.follow.APSendFollowService
import dev.usbharu.hideout.activitypub.service.activity.reject.ApSendRejectService
import dev.usbharu.hideout.activitypub.service.activity.undo.APSendUndoService
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.domain.model.relationship.Relationship
import dev.usbharu.hideout.core.domain.model.relationship.RelationshipRepository
import dev.usbharu.hideout.core.domain.model.user.User
import dev.usbharu.hideout.core.query.UserQueryService
import dev.usbharu.hideout.core.service.follow.SendFollowDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class RelationshipServiceImpl(
    private val applicationConfig: ApplicationConfig,
    private val userQueryService: UserQueryService,
    private val relationshipRepository: RelationshipRepository,
    private val apSendFollowService: APSendFollowService,
    private val apSendBlockService: APSendBlockService,
    private val apSendAcceptService: ApSendAcceptService,
    private val apSendRejectService: ApSendRejectService,
    private val apSendUndoService: APSendUndoService
) : RelationshipService {
    override suspend fun followRequest(userId: Long, targetId: Long) {
        val relationship =
            relationshipRepository.findByUserIdAndTargetUserId(userId, targetId)?.copy(followRequest = true)
                ?: Relationship(
                    userId = userId,
                    targetUserId = targetId,
                    following = false,
                    blocking = false,
                    muting = false,
                    followRequest = true,
                    ignoreFollowRequestFromTarget = false
                )

        val inverseRelationship = relationshipRepository.findByUserIdAndTargetUserId(targetId, userId) ?: Relationship(
            userId = targetId,
            targetUserId = userId,
            following = false,
            blocking = false,
            muting = false,
            followRequest = false,
            ignoreFollowRequestFromTarget = false
        )

        if (inverseRelationship.blocking) {
            logger.debug("FAILED Blocked by target. userId: {} targetId: {}", userId, targetId)
            return
        }

        if (relationship.blocking) {
            logger.debug("FAILED Blocking user. userId: {} targetId: {}", userId, targetId)
            return
        }
        if (inverseRelationship.ignoreFollowRequestFromTarget) {
            logger.debug("SUCCESS Ignore Follow Request. userId: {} targetId: {}", userId, targetId)
            return
        }


        relationshipRepository.save(relationship)

        val remoteUser = isRemoteUser(targetId)

        if (remoteUser != null) {
            val user = userQueryService.findById(userId)
            apSendFollowService.sendFollow(SendFollowDto(user, remoteUser))
        } else {
            //TODO: フォロー許可制ユーザーを実装したら消す
            acceptFollowRequest(userId, targetId)
        }

    }

    override suspend fun block(userId: Long, targetId: Long) {
        val relationship = relationshipRepository.findByUserIdAndTargetUserId(userId, targetId)
            ?.copy(blocking = true, followRequest = false, following = false) ?: Relationship(
            userId = userId,
            targetUserId = targetId,
            following = false,
            blocking = true,
            muting = false,
            followRequest = false,
            ignoreFollowRequestFromTarget = false
        )

        relationshipRepository.save(relationship)

        val remoteUser = isRemoteUser(targetId)

        if (remoteUser != null) {
            val user = userQueryService.findById(userId)
            apSendBlockService.sendBlock(user, remoteUser)
        }
    }

    override suspend fun acceptFollowRequest(userId: Long, targetId: Long) {
        val relationship = relationshipRepository.findByUserIdAndTargetUserId(userId, targetId)

        if (relationship == null) {
            logger.warn("FAILED Follow Request Not Found. (Relationship) userId: {} targetId: {}", userId, targetId)
            return
        }

        if (relationship.followRequest.not()) {
            logger.warn("FAILED Follow Request Not Found. (Follow Request) userId: {} targetId: {}", userId, targetId)
            return
        }

        if (relationship.blocking) {
            logger.warn("FAILED Blocking user userId: {} targetId: {}", userId, targetId)
            throw IllegalStateException("Cannot accept a follow request from a blocked user. userId: $userId targetId: $targetId")
        }

        val copy = relationship.copy(followRequest = false, following = true, blocking = false)

        relationshipRepository.save(copy)

        val remoteUser = isRemoteUser(targetId)

        if (remoteUser != null) {
            val user = userQueryService.findById(userId)
            apSendAcceptService.sendAccept(user, remoteUser)
        }
    }

    override suspend fun rejectFollowRequest(userId: Long, targetId: Long) {
        val relationship = relationshipRepository.findByUserIdAndTargetUserId(userId, targetId)

        if (relationship == null) {
            logger.warn("FAILED Follow Request Not Found. (Relationship) userId: {} targetId: {}", userId, targetId)
            return
        }

        if (relationship.followRequest.not()) {
            logger.warn("FAILED Follow Request Not Found. (Follow Request) userId: {} targetId: {}", userId, targetId)
            return
        }

        val copy = relationship.copy(followRequest = false, following = false, blocking = false)

        relationshipRepository.save(copy)

        val remoteUser = isRemoteUser(targetId)

        if (remoteUser != null) {
            val user = userQueryService.findById(userId)
            apSendRejectService.sendReject(user, remoteUser)
        }
    }

    override suspend fun ignoreFollowRequest(userId: Long, targetId: Long) {
        val relationship = relationshipRepository.findByUserIdAndTargetUserId(userId, targetId)
            ?.copy(ignoreFollowRequestFromTarget = true)
            ?: Relationship(
                userId = userId,
                targetUserId = targetId,
                following = false,
                blocking = false,
                muting = false,
                followRequest = false,
                ignoreFollowRequestFromTarget = true
            )

        relationshipRepository.save(relationship)
    }

    override suspend fun unfollow(userId: Long, targetId: Long) {
        val relationship = relationshipRepository.findByUserIdAndTargetUserId(userId, targetId)

        if (relationship == null) {
            logger.warn("FAILED Unfollow. (Relationship) userId: {} targetId: {}", userId, targetId)
            return
        }

        if (relationship.following.not()) {
            logger.warn("SUCCESS User already unfollow. userId: {} targetId: {}", userId, targetId)
            return
        }

        val copy = relationship.copy(following = false)

        relationshipRepository.save(copy)

        val remoteUser = isRemoteUser(targetId)

        if (remoteUser != null) {
            val user = userQueryService.findById(userId)
            apSendUndoService.sendUndoFollow(user, remoteUser)
        }
    }

    override suspend fun unblock(userId: Long, targetId: Long) {
        val relationship = relationshipRepository.findByUserIdAndTargetUserId(userId, targetId)

        if (relationship == null) {
            logger.warn("FAILED Unblock. (Relationship) userId: {} targetId: {}", userId, targetId)
            return
        }

        if (relationship.blocking.not()) {
            logger.warn("SUCCESS User is not blocking. userId: {] targetId: {}", userId, targetId)
            return
        }

        val copy = relationship.copy(blocking = false)
        relationshipRepository.save(copy)


        val remoteUser = isRemoteUser(targetId)
        if (remoteUser == null) {
            val user = userQueryService.findById(userId)
            apSendUndoService.sendUndoBlock(user, targetId)
        }
    }

    override suspend fun mute(userId: Long, targetId: Long) {
        val relationship = relationshipRepository.findByUserIdAndTargetUserId(userId, targetId)?.copy(muting = true)
            ?: Relationship(
                userId = userId,
                targetUserId = targetId,
                following = false,
                blocking = false,
                muting = true,
                followRequest = false,
                ignoreFollowRequestFromTarget = false
            )

        relationshipRepository.save(relationship)
    }

    override suspend fun unmute(userId: Long, targetId: Long) {
        val relationship = relationshipRepository.findByUserIdAndTargetUserId(userId, targetId)?.copy(muting = false)

        if (relationship == null) {
            logger.warn("FAILED Mute. (Relationship) userId: {} targetId: {}", userId, targetId)
            return
        }

        relationshipRepository.save(relationship)
    }

    private suspend fun isRemoteUser(userId: Long): User? {
        val user = try {
            userQueryService.findById(userId)
        } catch (e: FailedToGetResourcesException) {
            logger.warn("User not found.", e)
            throw IllegalStateException("User not found.", e)
        }

        if (user.domain == applicationConfig.url.host) {
            return null
        }
        return user
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RelationshipServiceImpl::class.java)
    }
}
