package dev.usbharu.hideout.activitypub.service.activity.like

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.activitypub.domain.model.Like
import dev.usbharu.hideout.activitypub.domain.model.Undo
import dev.usbharu.hideout.activitypub.service.common.APRequestService
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.external.job.DeliverRemoveReactionJob
import dev.usbharu.hideout.core.external.job.DeliverRemoveReactionJobParam
import dev.usbharu.hideout.core.query.ActorQueryService
import dev.usbharu.hideout.core.service.job.JobProcessor
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ApRemoveReactionJobProcessor(
    private val actorQueryService: ActorQueryService,
    private val transaction: Transaction,
    private val objectMapper: ObjectMapper,
    private val apRequestService: APRequestService,
    private val applicationConfig: ApplicationConfig
) : JobProcessor<DeliverRemoveReactionJobParam, DeliverRemoveReactionJob> {
    override suspend fun process(param: DeliverRemoveReactionJobParam): Unit = transaction.transaction {
        val like = objectMapper.readValue<Like>(param.like)

        val signer = actorQueryService.findByUrl(param.actor)

        apRequestService.apPost(
            param.inbox,
            Undo(
                actor = param.actor,
                apObject = like,
                id = "${applicationConfig.url}/undo/like/${param.id}",
                published = Instant.now().toString()
            ),
            signer
        )
    }

    override fun job(): DeliverRemoveReactionJob = DeliverRemoveReactionJob
}
