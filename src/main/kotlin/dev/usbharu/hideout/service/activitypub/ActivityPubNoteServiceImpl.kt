package dev.usbharu.hideout.service.activitypub

import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.PostEntity
import dev.usbharu.hideout.domain.model.ap.Note
import dev.usbharu.hideout.domain.model.job.DeliverPostJob
import dev.usbharu.hideout.plugins.postAp
import dev.usbharu.hideout.service.impl.UserService
import dev.usbharu.hideout.service.job.JobQueueParentService
import io.ktor.client.*
import kjob.core.job.JobProps

class ActivityPubNoteServiceImpl(
    private val httpClient: HttpClient,
    private val jobQueueParentService: JobQueueParentService,
    private val userService: UserService
) : ActivityPubNoteService {

    override suspend fun createNote(post: PostEntity) {
        val followers = userService.findFollowersById(post.userId)
        val userEntity = userService.findById(post.userId)
        val note = Config.configData.objectMapper.writeValueAsString(post)
        followers.forEach { followerEntity ->
            jobQueueParentService.schedule(DeliverPostJob) {
                props[it.actor] = userEntity.url
                props[it.post] = note
                props[it.inbox] = followerEntity.inbox
            }
        }
    }


    override suspend fun createNoteJob(props: JobProps<DeliverPostJob>) {
        val actor = props[DeliverPostJob.actor]
        val note = Config.configData.objectMapper.readValue<Note>(props[DeliverPostJob.post])
        val inbox = props[DeliverPostJob.inbox]
        httpClient.postAp(
            urlString = inbox,
            username = "$actor#pubkey",
            jsonLd = note
        )
    }
}
