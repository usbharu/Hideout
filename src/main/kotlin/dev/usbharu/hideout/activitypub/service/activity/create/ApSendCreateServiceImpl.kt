package dev.usbharu.hideout.activitypub.service.activity.create

import com.fasterxml.jackson.databind.ObjectMapper
import dev.usbharu.hideout.activitypub.domain.model.Create
import dev.usbharu.hideout.activitypub.query.NoteQueryService
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.external.job.DeliverPostJob
import dev.usbharu.hideout.core.query.FollowerQueryService
import dev.usbharu.hideout.core.query.UserQueryService
import dev.usbharu.hideout.core.service.job.JobQueueParentService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ApSendCreateServiceImpl(
    private val followerQueryService: FollowerQueryService,
    private val objectMapper: ObjectMapper,
    private val jobQueueParentService: JobQueueParentService,
    private val userQueryService: UserQueryService,
    private val noteQueryService: NoteQueryService,
    private val applicationConfig: ApplicationConfig
) : ApSendCreateService {
    override suspend fun createNote(post: Post) {
        logger.info("CREATE Create Local Note ${post.url}")
        logger.debug("START Create Local Note ${post.url}")
        logger.trace("{}", post)
        val followers = followerQueryService.findFollowersById(post.userId)

        logger.debug("DELIVER Deliver Note Create ${followers.size} accounts.")

        val userEntity = userQueryService.findById(post.userId)
        val note = noteQueryService.findById(post.id).first
        val create = Create(
            name = "Create Note",
            `object` = note,
            actor = note.attributedTo,
            id = "${applicationConfig.url}/create/note/${post.id}"
        )
        followers.forEach { followerEntity ->
            jobQueueParentService.schedule(DeliverPostJob) {
                props[DeliverPostJob.actor] = userEntity.url
                props[DeliverPostJob.inbox] = followerEntity.inbox
                props[DeliverPostJob.create] = objectMapper.writeValueAsString(create)
            }
        }

        logger.debug("SUCCESS Create Local Note ${post.url}")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ApSendCreateServiceImpl::class.java)
    }
}
