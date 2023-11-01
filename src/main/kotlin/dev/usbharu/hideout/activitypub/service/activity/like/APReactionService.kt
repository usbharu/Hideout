package dev.usbharu.hideout.activitypub.service.activity.like

import com.fasterxml.jackson.databind.ObjectMapper
import dev.usbharu.hideout.core.domain.model.reaction.Reaction
import dev.usbharu.hideout.core.external.job.DeliverReactionJob
import dev.usbharu.hideout.core.external.job.DeliverRemoveReactionJob
import dev.usbharu.hideout.core.query.FollowerQueryService
import dev.usbharu.hideout.core.query.PostQueryService
import dev.usbharu.hideout.core.query.UserQueryService
import dev.usbharu.hideout.core.service.job.JobQueueParentService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

interface APReactionService {
    suspend fun reaction(like: Reaction)
    suspend fun removeReaction(like: Reaction)
}

@Service
class APReactionServiceImpl(
    private val jobQueueParentService: JobQueueParentService,
    private val userQueryService: UserQueryService,
    private val followerQueryService: FollowerQueryService,
    private val postQueryService: PostQueryService,
    @Qualifier("activitypub") private val objectMapper: ObjectMapper
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
}
