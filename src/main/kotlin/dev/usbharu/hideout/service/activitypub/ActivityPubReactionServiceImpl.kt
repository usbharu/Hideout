package dev.usbharu.hideout.service.activitypub

import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.ap.Like
import dev.usbharu.hideout.domain.model.hideout.entity.Reaction
import dev.usbharu.hideout.domain.model.job.DeliverReactionJob
import dev.usbharu.hideout.exception.PostNotFoundException
import dev.usbharu.hideout.plugins.postAp
import dev.usbharu.hideout.repository.IPostRepository
import dev.usbharu.hideout.service.job.JobQueueParentService
import dev.usbharu.hideout.service.user.IUserService
import io.ktor.client.*
import kjob.core.job.JobProps
import org.koin.core.annotation.Single

@Single
class ActivityPubReactionServiceImpl(
    private val userService: IUserService,
    private val jobQueueParentService: JobQueueParentService,
    private val iPostRepository: IPostRepository,
    private val httpClient: HttpClient
) : ActivityPubReactionService {
    override suspend fun reaction(like: Reaction) {
        val followers = userService.findFollowersById(like.userId)
        val user = userService.findById(like.userId)
        val post =
            iPostRepository.findOneById(like.postId) ?: throw PostNotFoundException("${like.postId} was not found.")
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
}
