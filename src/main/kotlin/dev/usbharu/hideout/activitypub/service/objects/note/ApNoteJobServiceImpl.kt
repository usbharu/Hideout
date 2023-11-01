package dev.usbharu.hideout.activitypub.service.objects.note

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.activitypub.domain.model.Create
import dev.usbharu.hideout.activitypub.domain.model.Document
import dev.usbharu.hideout.activitypub.domain.model.Note
import dev.usbharu.hideout.activitypub.service.common.APRequestService
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.model.media.Media
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.external.job.DeliverPostJob
import dev.usbharu.hideout.core.query.UserQueryService
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
            objectMapper.readValue<List<Media>>(
                props[DeliverPostJob.media]
            )

        transaction.transaction {
            val signer = userQueryService.findByUrl(actor)
            val note = Note(
                name = "Note",
                id = postEntity.url,
                attributedTo = actor,
                content = postEntity.text,
                published = Instant.ofEpochMilli(postEntity.createdAt).toString(),
                to = listOfNotNull(APNoteServiceImpl.public, signer.followers),
                attachment = mediaList.map { Document(mediaType = "image/jpeg", url = it.url) }

            )
            val inbox = props[DeliverPostJob.inbox]
            logger.debug("createNoteJob: actor={}, note={}, inbox={}", actor, postEntity, inbox)
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
