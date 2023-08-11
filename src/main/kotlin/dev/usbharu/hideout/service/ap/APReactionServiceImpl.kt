package dev.usbharu.hideout.service.ap

import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.ap.Like
import dev.usbharu.hideout.domain.model.ap.Undo
import dev.usbharu.hideout.domain.model.hideout.entity.Reaction
import dev.usbharu.hideout.domain.model.job.DeliverReactionJob
import dev.usbharu.hideout.domain.model.job.DeliverRemoveReactionJob
import dev.usbharu.hideout.plugins.postAp
import dev.usbharu.hideout.query.FollowerQueryService
import dev.usbharu.hideout.query.PostQueryService
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.repository.IPostRepository
import dev.usbharu.hideout.service.job.JobQueueParentService
import io.ktor.client.*
import kjob.core.job.JobProps
import org.koin.core.annotation.Single
import java.time.Instant

@Single
class APReactionServiceImpl(
    private val jobQueueParentService: JobQueueParentService,
    private val iPostRepository: IPostRepository,
    private val httpClient: HttpClient,
    private val userQueryService: UserQueryService,
    private val followerQueryService: FollowerQueryService,
    private val postQueryService: PostQueryService
) : APReactionService {
    override suspend fun reaction(like: Reaction) {
        val followers = followerQueryService.findFollowersById(like.userId)
        val user = userQueryService.findById(like.userId)
        val post =
            postQueryService.findById(like.postId)
        followers.forEach { follower ->
            jobQueueParentService.schedule(DeliverReactionJob) {
                props[it.actor] = user.url
                props[it.reaction] = "❤"
                props[it.inbox] = follower.inbox
                props[it.postUrl] = post.url
                props[it.id] = post.id.toString()
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
                props[it.actor] = user.url
                props[it.inbox] = follower.inbox
                props[it.id] = post.id.toString()
                props[it.like] = Config.configData.objectMapper.writeValueAsString(like)
            }
        }
    }

    override suspend fun reactionJob(props: JobProps<DeliverReactionJob>) {
        val inbox = props[DeliverReactionJob.inbox]
        val actor = props[DeliverReactionJob.actor]
        val postUrl = props[DeliverReactionJob.postUrl]
        val id = props[DeliverReactionJob.id]
        val content = props[DeliverReactionJob.reaction]
        httpClient.postAp(
            urlString = inbox,
            username = "$actor#pubkey",
            jsonLd = Like(
                name = "Like",
                actor = actor,
                `object` = postUrl,
                id = "${Config.configData.url}/like/note/$id",
                content = content
            )
        )
    }

    override suspend fun removeReactionJob(props: JobProps<DeliverRemoveReactionJob>) {
        val inbox = props[DeliverRemoveReactionJob.inbox]
        val actor = props[DeliverRemoveReactionJob.actor]
        val like = Config.configData.objectMapper.readValue<Like>(props[DeliverRemoveReactionJob.like])
        httpClient.postAp(
            urlString = inbox,
            username = "$actor#pubkey",
            jsonLd = Undo(
                name = "Undo Reaction",
                actor = actor,
                `object` = like,
                id = "${Config.configData.url}/undo/note/${like.id}",
                published = Instant.now()
            )
        )
    }
}
