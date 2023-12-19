package dev.usbharu.hideout.activitypub.service.activity.create

import com.fasterxml.jackson.databind.ObjectMapper
import dev.usbharu.hideout.activitypub.domain.model.Create
import dev.usbharu.hideout.activitypub.query.NoteQueryService
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.exception.resource.PostNotFoundException
import dev.usbharu.hideout.core.domain.exception.resource.UserNotFoundException
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.external.job.DeliverPostJob
import dev.usbharu.hideout.core.query.FollowerQueryService
import dev.usbharu.hideout.core.service.job.JobQueueParentService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ApSendCreateServiceImpl(
    private val followerQueryService: FollowerQueryService,
    private val objectMapper: ObjectMapper,
    private val jobQueueParentService: JobQueueParentService,
    private val noteQueryService: NoteQueryService,
    private val applicationConfig: ApplicationConfig,
    private val actorRepository: ActorRepository
) : ApSendCreateService {
    override suspend fun createNote(post: Post) {
        logger.info("CREATE Create Local Note ${post.url}")
        logger.debug("START Create Local Note ${post.url}")
        logger.trace("{}", post)
        val followers = followerQueryService.findFollowersById(post.actorId)

        logger.debug("DELIVER Deliver Note Create ${followers.size} accounts.")

        val userEntity = actorRepository.findById(post.actorId) ?: throw UserNotFoundException.withId(post.actorId)
        val note = noteQueryService.findById(post.id)?.first ?: throw PostNotFoundException.withId(post.id)
        val create = Create(
            name = "Create Note",
            apObject = note,
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
