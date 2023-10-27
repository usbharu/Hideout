package dev.usbharu.hideout.service.ap.job

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.config.ApplicationConfig
import dev.usbharu.hideout.domain.model.ap.Create
import dev.usbharu.hideout.domain.model.ap.Document
import dev.usbharu.hideout.domain.model.ap.Note
import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.domain.model.job.DeliverPostJob
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.service.ap.APNoteServiceImpl
import dev.usbharu.hideout.service.ap.APRequestService
import dev.usbharu.hideout.service.core.Transaction
import kjob.core.job.JobProps
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class ApNoteJobServiceImpl(
    private val userQueryService: UserQueryService,
    private val apRequestService: APRequestService,
    @Qualifier("activitypub") private val objectMapper: ObjectMapper,
    private val transaction: Transaction,
    private val applicationConfig: ApplicationConfig
) : ApNoteJobService {
    override suspend fun createNoteJob(props: JobProps<DeliverPostJob>) {
        val actor = props[DeliverPostJob.actor]
        val postEntity = objectMapper.readValue<Post>(props[DeliverPostJob.post])
        val mediaList =
            objectMapper.readValue<List<dev.usbharu.hideout.domain.model.hideout.entity.Media>>(
                props[DeliverPostJob.media]
            )
        val note = Note(
            name = "Note",
            id = postEntity.url,
            attributedTo = actor,
            content = postEntity.text,
            published = Instant.ofEpochMilli(postEntity.createdAt).toString(),
            to = listOf(APNoteServiceImpl.public, "$actor/follower"),
            attachment = mediaList.map { Document(mediaType = "image/jpeg", url = it.url) }

        )
        val inbox = props[DeliverPostJob.inbox]
        logger.debug("createNoteJob: actor={}, note={}, inbox={}", actor, postEntity, inbox)

        transaction.transaction {
            val signer = userQueryService.findByUrl(actor)
            apRequestService.apPost(
                inbox,
                Create(
                    name = "Create Note",
                    `object` = note,
                    actor = note.attributedTo,
                    id = "${applicationConfig.url}/create/note/${postEntity.id}"
                ),
                signer
            )
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ApNoteJobServiceImpl::class.java)
    }
}
