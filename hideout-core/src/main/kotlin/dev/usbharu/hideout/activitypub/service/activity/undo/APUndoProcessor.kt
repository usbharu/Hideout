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

package dev.usbharu.hideout.activitypub.service.activity.undo

import dev.usbharu.hideout.activitypub.domain.model.*
import dev.usbharu.hideout.activitypub.domain.model.objects.ObjectValue
import dev.usbharu.hideout.activitypub.service.common.AbstractActivityPubProcessor
import dev.usbharu.hideout.activitypub.service.common.ActivityPubProcessContext
import dev.usbharu.hideout.activitypub.service.common.ActivityType
import dev.usbharu.hideout.activitypub.service.objects.user.APUserService
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.exception.resource.PostNotFoundException
import dev.usbharu.hideout.core.domain.exception.resource.UserNotFoundException
import dev.usbharu.hideout.core.domain.exception.resource.local.LocalUserNotFoundException
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.service.post.PostService
import dev.usbharu.hideout.core.service.reaction.ReactionService
import dev.usbharu.hideout.core.service.relationship.RelationshipService
import org.springframework.stereotype.Service

@Service
class APUndoProcessor(
    transaction: Transaction,
    private val apUserService: APUserService,
    private val relationshipService: RelationshipService,
    private val reactionService: ReactionService,
    private val actorRepository: ActorRepository,
    private val postRepository: PostRepository,
    private val postService: PostService
) : AbstractActivityPubProcessor<Undo>(transaction) {
    override suspend fun internalProcess(activity: ActivityPubProcessContext<Undo>) {
        val undo = activity.activity

        val type = undo.apObject.type.firstOrNull {
            it == "Block" || it == "Follow" || it == "Like" || it == "Announce" || it == "Accept"
        } ?: return

        when (type) {
            "Follow" -> {
                follow(undo)
                return
            }

            "Accept" -> {
                accept(undo)
                return
            }

            "Like" -> {
                like(undo)
                return
            }

            "Announce" -> {
                announce(undo)
                return
            }

            else -> {}
        }
        TODO()
    }

    private suspend fun accept(undo: Undo) {
        val accept = undo.apObject as Accept

        val acceptObject = if (accept.apObject is ObjectValue) {
            accept.apObject.`object`
        } else if (accept.apObject is Follow) {
            accept.apObject.apObject
        } else {
            logger.warn("FAILED Unsupported type. Undo Accept {}", accept.apObject.type)
            return
        }

        val accepter = apUserService.fetchPersonWithEntity(undo.actor, acceptObject).second
        val target = actorRepository.findByUrl(acceptObject) ?: throw UserNotFoundException.withUrl(acceptObject)

        relationshipService.rejectFollowRequest(accepter.id, target.id)
        return
    }

    private suspend fun like(undo: Undo) {
        val like = undo.apObject as Like

        val post = postRepository.findByUrl(like.apObject) ?: throw PostNotFoundException.withUrl(like.apObject)

        val signer = actorRepository.findById(post.actorId) ?: throw LocalUserNotFoundException.withId(post.actorId)
        val actor = apUserService.fetchPersonWithEntity(like.actor, signer.url).second

        reactionService.receiveRemoveReaction(actor.id, post.id)
        return
    }

    private suspend fun follow(undo: Undo) {
        val follow = undo.apObject as Follow

        val follower = apUserService.fetchPersonWithEntity(undo.actor, follow.apObject).second
        val target = actorRepository.findByUrl(follow.apObject) ?: throw UserNotFoundException.withUrl(follow.apObject)

        relationshipService.unfollow(follower.id, target.id)
        return
    }

    private suspend fun announce(undo: Undo) {
        val announce = undo.apObject as Announce

        val findByApId = postRepository.findByApId(announce.id) ?: return
        postService.deleteRemote(findByApId)
    }

    override fun isSupported(activityType: ActivityType): Boolean = activityType == ActivityType.Undo

    override fun type(): Class<Undo> = Undo::class.java
}
