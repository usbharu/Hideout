package dev.usbharu.hideout.core.service.relationship

import dev.usbharu.hideout.activitypub.service.activity.accept.ApSendAcceptService
import dev.usbharu.hideout.activitypub.service.activity.block.APSendBlockService
import dev.usbharu.hideout.activitypub.service.activity.follow.APSendFollowService
import dev.usbharu.hideout.activitypub.service.activity.reject.ApSendRejectService
import dev.usbharu.hideout.activitypub.service.activity.undo.APSendUndoService
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.exception.resource.UserNotFoundException
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.relationship.Relationship
import dev.usbharu.hideout.core.domain.model.relationship.RelationshipRepository
import dev.usbharu.hideout.core.service.follow.SendFollowDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class RelationshipServiceImpl(
    private val applicationConfig: ApplicationConfig,
    private val relationshipRepository: RelationshipRepository,
    private val apSendFollowService: APSendFollowService,
    private val apSendBlockService: APSendBlockService,
    private val apSendAcceptService: ApSendAcceptService,
    private val apSendRejectService: ApSendRejectService,
    private val apSendUndoService: APSendUndoService,
    private val actorRepository: ActorRepository
) : RelationshipService {
    override suspend fun followRequest(actorId: Long, targetId: Long) {
        logger.info("START Follow Request userId: {} targetId: {}", actorId, targetId)

        val relationship =
            relationshipRepository.findByUserIdAndTargetUserId(actorId, targetId)?.copy(followRequest = true)
                ?: Relationship(
                    actorId = actorId,
                    targetActorId = targetId,
                    following = false,
                    blocking = false,
                    muting = false,
                    followRequest = true,
                    ignoreFollowRequestToTarget = false
                )

        val inverseRelationship = relationshipRepository.findByUserIdAndTargetUserId(targetId, actorId) ?: Relationship(
            actorId = targetId,
            targetActorId = actorId,
            following = false,
            blocking = false,
            muting = false,
            followRequest = false,
            ignoreFollowRequestToTarget = false
        )

        if (inverseRelationship.blocking) {
            logger.debug("FAILED Blocked by target. userId: {} targetId: {}", actorId, targetId)
            return
        }

        if (relationship.blocking) {
            logger.debug("FAILED Blocking user. userId: {} targetId: {}", actorId, targetId)
            return
        }
        if (relationship.ignoreFollowRequestToTarget) {
            logger.debug("SUCCESS Ignore Follow Request. userId: {} targetId: {}", actorId, targetId)
            return
        }

        if (relationship.following) {
            logger.debug("SUCCESS User already follow. userId: {} targetId: {}", actorId, targetId)
            acceptFollowRequest(targetId, actorId, true)
            return
        }

        relationshipRepository.save(relationship)

        val remoteUser = isRemoteUser(targetId)

        if (remoteUser != null) {
            val user = actorRepository.findById(actorId) ?: throw UserNotFoundException.withId(actorId)
            apSendFollowService.sendFollow(SendFollowDto(user, remoteUser))
        } else {
            val target = actorRepository.findById(targetId) ?: throw UserNotFoundException.withId(targetId)
            if (target.locked.not()) {
                acceptFollowRequest(targetId, actorId)
            }
        }

        logger.info("SUCCESS Follow Request userId: {} targetId: {}", actorId, targetId)
    }

    override suspend fun block(actorId: Long, targetId: Long) {
        val relationship = relationshipRepository.findByUserIdAndTargetUserId(actorId, targetId)

        val user = actorRepository.findById(actorId) ?: throw UserNotFoundException.withId(actorId)
        val targetActor = actorRepository.findById(targetId) ?: throw UserNotFoundException.withId(targetId)
        if (relationship?.following == true) {
            actorRepository.save(user.decrementFollowing())
            actorRepository.save(targetActor.decrementFollowers())
        }

        val blockedRelationship = relationship
            ?.copy(blocking = true, followRequest = false, following = false) ?: Relationship(
            actorId = actorId,
            targetActorId = targetId,
            following = false,
            blocking = true,
            muting = false,
            followRequest = false,
            ignoreFollowRequestToTarget = false
        )

        val inverseRelationship = relationshipRepository.findByUserIdAndTargetUserId(targetId, actorId)

        if (inverseRelationship?.following == true) {
            actorRepository.save(targetActor.decrementFollowing())
            actorRepository.save(user.decrementFollowers())
        }

        val blockedInverseRelationship = inverseRelationship
            ?.copy(followRequest = false, following = false)

        relationshipRepository.save(blockedRelationship)
        if (blockedInverseRelationship != null) {
            relationshipRepository.save(blockedInverseRelationship)
        }

        val remoteUser = isRemoteUser(targetId)

        if (remoteUser != null) {
            apSendBlockService.sendBlock(user, remoteUser)
        }
    }

    override suspend fun acceptFollowRequest(actorId: Long, targetId: Long, force: Boolean) {
        logger.info("START Accept follow request userId: {} targetId: {}", actorId, targetId)

        val relationship = relationshipRepository.findByUserIdAndTargetUserId(targetId, actorId)

        val inverseRelationship = relationshipRepository.findByUserIdAndTargetUserId(actorId, targetId) ?: Relationship(
            actorId = targetId,
            targetActorId = actorId,
            following = false,
            blocking = false,
            muting = false,
            followRequest = false,
            ignoreFollowRequestToTarget = false
        )

        if (relationship == null) {
            logger.warn("FAILED Follow Request Not Found. (Relationship) userId: {} targetId: {}", actorId, targetId)
            return
        }

        if (relationship.followRequest.not() && force.not()) {
            logger.warn("FAILED Follow Request Not Found. (Follow Request) userId: {} targetId: {}", actorId, targetId)
            return
        }

        if (relationship.blocking) {
            logger.warn("FAILED Blocking user userId: {} targetId: {}", actorId, targetId)
            throw IllegalStateException(
                "Cannot accept a follow request from a blocked user. userId: $actorId targetId: $targetId"
            )
        }

        if (inverseRelationship.blocking) {
            logger.warn("FAILED BLocked by user userId: {} targetId: {}", actorId, targetId)
            throw IllegalStateException(
                "Cannot accept a follow request from a blocking user. userId: $actorId targetId: $targetId"
            )
        }

        val copy = relationship.copy(followRequest = false, following = true, blocking = false)

        val user = actorRepository.findById(actorId) ?: throw UserNotFoundException.withId(actorId)

        actorRepository.save(user.incrementFollowers())

        relationshipRepository.save(copy)

        val remoteActor = actorRepository.findById(targetId) ?: throw UserNotFoundException.withId(targetId)

        actorRepository.save(remoteActor.incrementFollowing())

        if (isRemoteActor(remoteActor)) {
            apSendAcceptService.sendAcceptFollow(user, remoteActor)
        }
    }

    override suspend fun rejectFollowRequest(actorId: Long, targetId: Long) {
        val relationship = relationshipRepository.findByUserIdAndTargetUserId(targetId, actorId)

        if (relationship == null) {
            logger.warn("FAILED Follow Request Not Found. (Relationship) userId: {} targetId: {}", actorId, targetId)
            return
        }

        if (relationship.followRequest.not() && relationship.following.not()) {
            logger.warn("FAILED Follow Request Not Found. (Follow Request) userId: {} targetId: {}", actorId, targetId)
            return
        }

        val copy = relationship.copy(followRequest = false, following = false)

        relationshipRepository.save(copy)

        val remoteUser = isRemoteUser(targetId)

        if (remoteUser != null) {
            val user = actorRepository.findById(actorId) ?: throw UserNotFoundException.withId(actorId)
            apSendRejectService.sendRejectFollow(user, remoteUser)
        }
    }

    override suspend fun ignoreFollowRequest(actorId: Long, targetId: Long) {
        val relationship = relationshipRepository.findByUserIdAndTargetUserId(targetId, actorId)
            ?.copy(ignoreFollowRequestToTarget = true)
            ?: Relationship(
                actorId = targetId,
                targetActorId = actorId,
                following = false,
                blocking = false,
                muting = false,
                followRequest = false,
                ignoreFollowRequestToTarget = true
            )

        relationshipRepository.save(relationship)
    }

    override suspend fun unfollow(actorId: Long, targetId: Long) {
        val relationship = relationshipRepository.findByUserIdAndTargetUserId(actorId, targetId)

        if (relationship == null) {
            logger.warn("FAILED Unfollow. (Relationship) userId: {} targetId: {}", actorId, targetId)
            return
        }

        val user = actorRepository.findById(actorId) ?: throw UserNotFoundException.withId(actorId)
        val targetActor = actorRepository.findById(targetId) ?: throw UserNotFoundException.withId(targetId)

        if (relationship.following) {
            actorRepository.save(user.decrementFollowing())
            actorRepository.save(targetActor.decrementFollowers())
        }

        if (relationship.following.not()) {
            logger.warn("SUCCESS User already unfollow. userId: {} targetId: {}", actorId, targetId)
            return
        }

        val copy = relationship.copy(following = false)

        relationshipRepository.save(copy)

        val remoteUser = isRemoteUser(targetId)

        if (remoteUser != null) {
            apSendUndoService.sendUndoFollow(user, remoteUser)
        }
    }

    override suspend fun unblock(actorId: Long, targetId: Long) {
        val relationship = relationshipRepository.findByUserIdAndTargetUserId(actorId, targetId)

        if (relationship == null) {
            logger.warn("FAILED Unblock. (Relationship) userId: {} targetId: {}", actorId, targetId)
            return
        }

        if (relationship.blocking.not()) {
            logger.warn("SUCCESS User is not blocking. userId: {] targetId: {}", actorId, targetId)
            return
        }

        val copy = relationship.copy(blocking = false)
        relationshipRepository.save(copy)

        val remoteUser = isRemoteUser(targetId)
        if (remoteUser != null) {
            val user = actorRepository.findById(actorId) ?: throw UserNotFoundException.withId(actorId)
            apSendUndoService.sendUndoBlock(user, remoteUser)
        }
    }

    override suspend fun mute(actorId: Long, targetId: Long) {
        val relationship = relationshipRepository.findByUserIdAndTargetUserId(actorId, targetId)?.copy(muting = true)
            ?: Relationship(
                actorId = actorId,
                targetActorId = targetId,
                following = false,
                blocking = false,
                muting = true,
                followRequest = false,
                ignoreFollowRequestToTarget = false
            )

        relationshipRepository.save(relationship)
    }

    override suspend fun unmute(actorId: Long, targetId: Long) {
        val relationship = relationshipRepository.findByUserIdAndTargetUserId(actorId, targetId)?.copy(muting = false)

        if (relationship == null) {
            logger.warn("FAILED Mute. (Relationship) userId: {} targetId: {}", actorId, targetId)
            return
        }

        relationshipRepository.save(relationship)
    }

    private fun isRemoteActor(actor: Actor): Boolean = actor.domain != applicationConfig.url.host

    private suspend fun isRemoteUser(userId: Long): Actor? {
        logger.trace("isRemoteUser({})", userId)
        val user =
            actorRepository.findById(userId) ?: throw UserNotFoundException.withId(userId)

        logger.trace("user info {}", user)

        if (user.domain == applicationConfig.url.host) {
            logger.trace("user: {} is local user", userId)
            return null
        }
        logger.trace("user: {} is remote user", userId)
        return user
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RelationshipServiceImpl::class.java)
    }
}
