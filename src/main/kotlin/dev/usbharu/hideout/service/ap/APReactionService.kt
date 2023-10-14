package dev.usbharu.hideout.service.ap

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.config.ApplicationConfig
import dev.usbharu.hideout.domain.model.ap.Like
import dev.usbharu.hideout.domain.model.ap.Undo
import dev.usbharu.hideout.domain.model.hideout.entity.Reaction
import dev.usbharu.hideout.domain.model.job.DeliverReactionJob
import dev.usbharu.hideout.domain.model.job.DeliverRemoveReactionJob
import dev.usbharu.hideout.query.FollowerQueryService
import dev.usbharu.hideout.query.PostQueryService
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.service.job.JobQueueParentService
import io.ktor.client.*
import kjob.core.job.JobProps
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.time.Instant

interface APReactionService {
    suspend fun reaction(like: Reaction)
    suspend fun removeReaction(like: Reaction)
    suspend fun reactionJob(props: JobProps<DeliverReactionJob>)
    suspend fun removeReactionJob(props: JobProps<DeliverRemoveReactionJob>)
}

@Service
class APReactionServiceImpl(
    private val jobQueueParentService: JobQueueParentService,
    private val httpClient: HttpClient,
    private val userQueryService: UserQueryService,
    private val followerQueryService: FollowerQueryService,
    private val postQueryService: PostQueryService,
    @Qualifier("activitypub") private val objectMapper: ObjectMapper,
    private val applicationConfig: ApplicationConfig,
    private val apRequestService: APRequestService
) : APReactionService {
    override suspend fun reaction(like: Reaction) {
        val followers = followerQueryService.findFollowersById(like.userId)
        val user = userQueryService.findById(like.userId)
        val post =
            postQueryService.findById(like.postId)
        followers.forEach { follower ->
            jobQueueParentService.schedule(DeliverReactionJob) {
                props[DeliverReactionJob.actor] = user.url
                props[DeliverReactionJob.reaction] = "❤"
                props[DeliverReactionJob.inbox] = follower.inbox
                props[DeliverReactionJob.postUrl] = post.url
                props[DeliverReactionJob.id] = post.id.toString()
            }
        }
    }

    override suspend fun removeReaction(like: Reaction) {
        val followers = followerQueryService.findFollowersById(like.userId)
        val user = userQueryService.findById(like.userId)
        val post =
            postQueryService.findById(like.postId)
        followers.forEach { follower ->
            jobQueueParentService.schedule(DeliverRemoveReactionJob) {
                props[DeliverRemoveReactionJob.actor] = user.url
                props[DeliverRemoveReactionJob.inbox] = follower.inbox
                props[DeliverRemoveReactionJob.id] = post.id.toString()
                props[DeliverRemoveReactionJob.like] = objectMapper.writeValueAsString(like)
            }
        }
    }

    override suspend fun reactionJob(props: JobProps<DeliverReactionJob>) {
        val inbox = props[DeliverReactionJob.inbox]
        val actor = props[DeliverReactionJob.actor]
        val postUrl = props[DeliverReactionJob.postUrl]
        val id = props[DeliverReactionJob.id]
        val content = props[DeliverReactionJob.reaction]

        val signer = userQueryService.findByUrl(actor)

        apRequestService.apPost(
            inbox, Like(
                name = "Like",
                actor = actor,
                `object` = postUrl,
                id = "${applicationConfig.url}/like/note/$id",
                content = content
            ), signer
        )
    }

    override suspend fun removeReactionJob(props: JobProps<DeliverRemoveReactionJob>) {
        val inbox = props[DeliverRemoveReactionJob.inbox]
        val actor = props[DeliverRemoveReactionJob.actor]
        val like = objectMapper.readValue<Like>(props[DeliverRemoveReactionJob.like])

        val signer = userQueryService.findByUrl(actor)

        apRequestService.apPost(
            inbox, Undo(
                name = "Undo Reaction",
                actor = actor,
                `object` = like,
                id = "${applicationConfig.url}/undo/note/${like.id}",
                published = Instant.now()
            ), signer
        )
    }
}
