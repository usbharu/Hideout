package dev.usbharu.hideout.activitypub.service.activity.like

import dev.usbharu.hideout.activitypub.domain.model.Like
import dev.usbharu.hideout.activitypub.service.common.APRequestService
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.external.job.DeliverReactionJob
import dev.usbharu.hideout.core.external.job.DeliverReactionJobParam
import dev.usbharu.hideout.core.query.ActorQueryService
import dev.usbharu.hideout.core.service.job.JobProcessor
import org.springframework.stereotype.Service

@Service
class ApReactionJobProcessor(
    private val actorQueryService: ActorQueryService,
    private val apRequestService: APRequestService,
    private val applicationConfig: ApplicationConfig,
    private val transaction: Transaction
) : JobProcessor<DeliverReactionJobParam, DeliverReactionJob> {
    override suspend fun process(param: DeliverReactionJobParam): Unit = transaction.transaction {
        val signer = actorQueryService.findByUrl(param.actor)

        apRequestService.apPost(
            param.inbox,
            Like(
                actor = param.actor,
                apObject = param.postUrl,
                id = "${applicationConfig.url}/liek/note/${param.id}",
                content = param.reaction
            ),
            signer
        )
    }

    override fun job(): DeliverReactionJob = DeliverReactionJob
}
