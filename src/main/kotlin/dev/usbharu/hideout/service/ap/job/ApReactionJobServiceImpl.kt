package dev.usbharu.hideout.service.ap.job

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.config.ApplicationConfig
import dev.usbharu.hideout.domain.model.ap.Like
import dev.usbharu.hideout.domain.model.ap.Undo
import dev.usbharu.hideout.domain.model.job.DeliverReactionJob
import dev.usbharu.hideout.domain.model.job.DeliverRemoveReactionJob
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.service.ap.APRequestService
import kjob.core.job.JobProps
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ApReactionJobServiceImpl(
    private val userQueryService: UserQueryService,
    private val apRequestService: APRequestService,
    private val applicationConfig: ApplicationConfig,
    @Qualifier("activitypub") private val objectMapper: ObjectMapper
) : ApReactionJobService {
    override suspend fun reactionJob(props: JobProps<DeliverReactionJob>) {
        val inbox = props[DeliverReactionJob.inbox]
        val actor = props[DeliverReactionJob.actor]
        val postUrl = props[DeliverReactionJob.postUrl]
        val id = props[DeliverReactionJob.id]
        val content = props[DeliverReactionJob.reaction]

        val signer = userQueryService.findByUrl(actor)

        apRequestService.apPost(
            inbox,
            Like(
                name = "Like",
                actor = actor,
                `object` = postUrl,
                id = "${applicationConfig.url}/like/note/$id",
                content = content
            ),
            signer
        )
    }

    override suspend fun removeReactionJob(props: JobProps<DeliverRemoveReactionJob>) {
        val inbox = props[DeliverRemoveReactionJob.inbox]
        val actor = props[DeliverRemoveReactionJob.actor]
        val like = objectMapper.readValue<Like>(props[DeliverRemoveReactionJob.like])

        val signer = userQueryService.findByUrl(actor)

        apRequestService.apPost(
            inbox,
            Undo(
                name = "Undo Reaction",
                actor = actor,
                `object` = like,
                id = "${applicationConfig.url}/undo/note/${like.id}",
                published = Instant.now()
            ),
            signer
        )
    }
}
