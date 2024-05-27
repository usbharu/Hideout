/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.usbharu.hideout.core.service.user

import dev.usbharu.hideout.activitypub.service.activity.delete.APSendDeleteService
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.exception.resource.DuplicateException
import dev.usbharu.hideout.core.domain.exception.resource.UserNotFoundException
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.deletedActor.DeletedActor
import dev.usbharu.hideout.core.domain.model.deletedActor.DeletedActorRepository
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.reaction.ReactionRepository
import dev.usbharu.hideout.core.domain.model.relationship.RelationshipRepository
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetail
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import dev.usbharu.hideout.core.external.job.UpdateActorTask
import dev.usbharu.hideout.core.service.instance.InstanceService
import dev.usbharu.hideout.core.service.post.PostService
import dev.usbharu.owl.producer.api.OwlProducer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
@Suppress("LongParameterList")
class UserServiceImpl(
    private val actorRepository: ActorRepository,
    private val userAuthService: UserAuthService,
    private val actorBuilder: Actor.UserBuilder,
    private val applicationConfig: ApplicationConfig,
    private val instanceService: InstanceService,
    private val userDetailRepository: UserDetailRepository,
    private val deletedActorRepository: DeletedActorRepository,
    private val reactionRepository: ReactionRepository,
    private val relationshipRepository: RelationshipRepository,
    private val postService: PostService,
    private val apSendDeleteService: APSendDeleteService,
    private val postRepository: PostRepository,
    private val owlProducer: OwlProducer,
) :
    UserService {

    override suspend fun usernameAlreadyUse(username: String): Boolean {
        val findByNameAndDomain = actorRepository.findByNameAndDomain(username, applicationConfig.url.host)
        return findByNameAndDomain != null
    }

    override suspend fun createLocalUser(user: UserCreateDto): Actor {
        if (applicationConfig.private) {
            throw IllegalStateException("Instance is a private mode.")
        }

        val nextId = actorRepository.nextId()
        val hashedPassword = userAuthService.hash(user.password)
        val keyPair = userAuthService.generateKeyPair()
        val userUrl = "${applicationConfig.url}/users/${user.name}"
        val userEntity = actorBuilder.of(
            id = nextId,
            name = user.name,
            domain = applicationConfig.url.host,
            screenName = user.screenName,
            description = user.description,
            inbox = "$userUrl/inbox",
            outbox = "$userUrl/outbox",
            url = userUrl,
            publicKey = keyPair.public.toPem(),
            privateKey = keyPair.private.toPem(),
            createdAt = Instant.now(),
            following = "$userUrl/following",
            followers = "$userUrl/followers",
            keyId = "$userUrl#pubkey",
            locked = false,
            instance = 0
        )
        val save = actorRepository.save(userEntity)
        userDetailRepository.save(UserDetail(nextId, hashedPassword, true))
        return save
    }

    override suspend fun createRemoteUser(user: RemoteUserCreateDto, idOverride: Long?): Actor {
        logger.info("START Create New remote user. name: {} url: {}", user.name, user.url)

        val deletedActor = deletedActorRepository.findByNameAndDomain(user.name, user.domain)

        if (deletedActor != null) {
            logger.warn("FAILED Deleted actor. user: ${user.name} domain: ${user.domain}")
            throw IllegalStateException("Cannot create Deleted actor.")
        }

        val instance = instanceService.fetchInstance(user.url, user.sharedInbox)

        val nextId = actorRepository.nextId()
        val userEntity = actorBuilder.of(
            id = idOverride ?: nextId,
            name = user.name,
            domain = user.domain,
            screenName = user.screenName,
            description = user.description,
            inbox = user.inbox,
            outbox = user.outbox,
            url = user.url,
            publicKey = user.publicKey,
            createdAt = Instant.now(),
            followers = user.followers,
            following = user.following,
            keyId = user.keyId,
            instance = instance.id,
            locked = user.locked ?: false
        )
        return try {
            val save = actorRepository.save(userEntity)
            logger.warn("SUCCESS Create New remote user. id: {} name: {} url: {}", userEntity.id, user.name, user.url)
            save
        } catch (_: DuplicateException) {
            actorRepository.findByUrl(user.url)!!
        }
    }

    override suspend fun updateUser(userId: Long, updateUserDto: UpdateUserDto) {
        val userDetail = userDetailRepository.findByActorId(userId)
            ?: throw IllegalArgumentException("userId: $userId was not found.")

        val actor = actorRepository.findById(userId) ?: throw IllegalArgumentException("userId $userId was not found.")

        actorRepository.save(
            actor.copy(
                screenName = updateUserDto.screenName,
                description = updateUserDto.description,
                locked = updateUserDto.locked
            )
        )

        userDetailRepository.save(
            userDetail.copy(
                autoAcceptFolloweeFollowRequest = updateUserDto.autoAcceptFolloweeFollowRequest
            )
        )
    }

    override suspend fun deleteRemoteActor(actorId: Long) {
        val actor = actorRepository.findByIdWithLock(actorId) ?: throw UserNotFoundException.withId(actorId)
        val deletedActor = DeletedActor(
            id = actor.id,
            name = actor.name,
            domain = actor.domain,
            apId = actor.url,
            publicKey = actor.publicKey,
            deletedAt = Instant.now()
        )
        relationshipRepository.deleteByActorIdOrTargetActorId(actorId, actorId)

        reactionRepository.deleteByActorId(actorId)

        postService.deleteByActor(actorId)

        actorRepository.delete(actor.id)
        deletedActorRepository.save(deletedActor)
    }

    override suspend fun restorationRemoteActor(actorId: Long) {
        val deletedActor = deletedActorRepository.findById(actorId)
            ?: return

        deletedActorRepository.delete(deletedActor)

        owlProducer.publishTask(UpdateActorTask(deletedActor.id, deletedActor.apId))
    }

    override suspend fun deleteLocalUser(userId: Long) {
        val actor = actorRepository.findByIdWithLock(userId) ?: throw UserNotFoundException.withId(userId)
        apSendDeleteService.sendDeleteActor(actor)
        val deletedActor = DeletedActor(
            id = actor.id,
            name = actor.name,
            domain = actor.domain,
            apId = actor.url,
            publicKey = actor.publicKey,
            deletedAt = Instant.now()
        )
        relationshipRepository.deleteByActorIdOrTargetActorId(userId, userId)

        reactionRepository.deleteByActorId(actor.id)

        postService.deleteByActor(actor.id)
        actorRepository.delete(actor.id)
        val userDetail =
            userDetailRepository.findByActorId(actor.id) ?: throw IllegalStateException("user detail not found.")
        userDetailRepository.delete(userDetail)
        deletedActorRepository.save(deletedActor)
    }

    override suspend fun updateUserStatistics(userId: Long) {
        val actor = actorRepository.findByIdWithLock(userId) ?: throw UserNotFoundException.withId(userId)

        val followerCount = relationshipRepository.countByTargetIdAndFollowing(userId, true)
        val followingCount = relationshipRepository.countByUserIdAndFollowing(userId, true)
        val postsCount = postRepository.countByActorId(userId)

        actorRepository.save(
            actor.copy(
                followersCount = followerCount,
                followingCount = followingCount,
                postsCount = postsCount
            )
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserServiceImpl::class.java)
    }
}
