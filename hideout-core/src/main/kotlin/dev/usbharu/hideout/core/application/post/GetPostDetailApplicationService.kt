package dev.usbharu.hideout.core.application.post

import dev.usbharu.hideout.core.application.exception.InternalServerException
import dev.usbharu.hideout.core.application.exception.PermissionDeniedException
import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.instance.Instance
import dev.usbharu.hideout.core.domain.model.instance.InstanceRepository
import dev.usbharu.hideout.core.domain.model.media.Media
import dev.usbharu.hideout.core.domain.model.media.MediaRepository
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import dev.usbharu.hideout.core.domain.service.post.IPostReadAccessControl
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class GetPostDetailApplicationService(
    transaction: Transaction,
    private val postRepository: PostRepository,
    private val actorRepository: ActorRepository,
    private val instanceRepository: InstanceRepository,
    private val mediaRepository: MediaRepository,
    private val iPostReadAccessControl: IPostReadAccessControl
) : AbstractApplicationService<GetPostDetail, PostDetail>(
    transaction, logger
) {
    override suspend fun internalExecute(command: GetPostDetail, principal: Principal): PostDetail {
        val post = postRepository.findById(PostId(command.postId))
            ?: throw IllegalArgumentException("Post ${command.postId} not found.")
        if (iPostReadAccessControl.isAllow(post, principal).not()) {
            throw PermissionDeniedException()
        }
        val actor =
            actorRepository.findById(post.actorId) ?: throw InternalServerException("Actor ${post.actorId} not found.")
        val instance = instanceRepository.findById(post.instanceId)
            ?: throw InternalServerException("Instance ${post.instanceId} not found.")

        val iconMedia = actor.icon?.let { mediaRepository.findById(it) }

        val mediaList = mediaRepository.findByIds(post.mediaIds)

        return PostDetail.of(
            post,
            actor,
            instance,
            iconMedia,
            mediaList,
            post.replyId?.let { fetchChild(it, actor, instance, iconMedia, principal) },
            post.repostId?.let { fetchChild(it, actor, instance, iconMedia, principal) },
            post.moveTo?.let { fetchChild(it, actor, instance, iconMedia, principal) },
        )
    }

    private suspend fun fetchChild(
        postId: PostId,
        actor: Actor,
        instance: Instance,
        iconMedia: Media?,
        principal: Principal
    ): PostDetail? {
        val post = postRepository.findById(postId) ?: return null

        if (iPostReadAccessControl.isAllow(post, principal).not()) {
            throw PermissionDeniedException()
        }

        val (first, second: Instance, third) = if (actor.id != post.actorId) {
            Triple(
                actorRepository.findById(post.actorId) ?: return null,
                instanceRepository.findById(actor.instance) ?: return null,
                actor.icon?.let { mediaRepository.findById(it) }
            )
        } else {
            Triple(actor, instance, iconMedia)
        }

        val mediaList = mediaRepository.findByIds(post.mediaIds)
        return PostDetail.of(
            post, first, second, third, mediaList
        )

    }

    companion object {
        private val logger = LoggerFactory.getLogger(GetPostDetailApplicationService::class.java)
    }
}