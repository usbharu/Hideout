package dev.usbharu.hideout.activitypub.service.activity.like

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.activitypub.domain.model.Like
import dev.usbharu.hideout.activitypub.domain.model.Undo
import dev.usbharu.hideout.activitypub.service.common.APRequestService
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.external.job.DeliverReactionJob
import dev.usbharu.hideout.core.external.job.DeliverRemoveReactionJob
import dev.usbharu.hideout.core.query.UserQueryService
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
        val id = props[DeliverRemoveReactionJob.id]

        val signer = userQueryService.findByUrl(actor)

        apRequestService.apPost(
            inbox,
            Undo(
                name = "Undo Reaction",
                actor = actor,
                `object` = like,
                id = "${applicationConfig.url}/undo/note/$id",
                published = Instant.now()
            ),
            signer
        )
    }
}
