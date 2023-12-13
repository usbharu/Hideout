package dev.usbharu.hideout.activitypub.service.activity.delete

import dev.usbharu.hideout.activitypub.domain.model.Delete
import dev.usbharu.hideout.activitypub.domain.model.Tombstone
import dev.usbharu.hideout.activitypub.domain.model.objects.ObjectValue
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.external.job.DeliverDeleteJob
import dev.usbharu.hideout.core.external.job.DeliverDeleteJobParam
import dev.usbharu.hideout.core.query.ActorQueryService
import dev.usbharu.hideout.core.query.FollowerQueryService
import dev.usbharu.hideout.core.service.job.JobQueueParentService
import org.springframework.stereotype.Service
import java.time.Instant

interface APSendDeleteService {
    suspend fun sendDeleteNote(deletedPost: Post)
    suspend fun sendDeleteActor(deletedActor: Actor)
}

@Service
class APSendDeleteServiceImpl(
    private val jobQueueParentService: JobQueueParentService,
    private val delverDeleteJob: DeliverDeleteJob,
    private val followerQueryService: FollowerQueryService,
    private val actorQueryService: ActorQueryService,
    private val applicationConfig: ApplicationConfig
) : APSendDeleteService {
    override suspend fun sendDeleteNote(deletedPost: Post) {
        val actor = actorQueryService.findById(deletedPost.actorId)
        val followersById = followerQueryService.findFollowersById(deletedPost.actorId)

        val delete = Delete(
            actor = actor.url,
            id = "${applicationConfig.url}/delete/note/${deletedPost.id}",
            published = Instant.now().toString(),
            `object` = Tombstone(id = deletedPost.apId)
        )

        followersById.forEach {
            val jobProps = DeliverDeleteJobParam(
                delete,
                it.inbox,
                actor.id
            )
            jobQueueParentService.scheduleTypeSafe(delverDeleteJob, jobProps)
        }
    }

    override suspend fun sendDeleteActor(deletedActor: Actor) {
        val followers = followerQueryService.findFollowersById(deletedActor.id)

        val delete = Delete(
            actor = deletedActor.url,
            `object` = ObjectValue(emptyList(), `object` = deletedActor.url),
            id = "${applicationConfig.url}/delete/actor/${deletedActor.id}",
            published = Instant.now().toString()
        )

        followers.forEach {
            DeliverDeleteJobParam(
                delete = delete,
                it.inbox,
                deletedActor.id
            )
        }
    }

}
