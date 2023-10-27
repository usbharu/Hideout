package dev.usbharu.hideout.service.ap.job

import dev.usbharu.hideout.domain.model.job.*
import kjob.core.dsl.JobContextWithProps
import kjob.core.job.JobProps
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ApJobServiceImpl(
    private val apReceiveFollowJobService: APReceiveFollowJobService,
    private val apNoteJobService: ApNoteJobService,
    private val apReactionJobService: ApReactionJobService
) : ApJobService {
    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    override suspend fun <T : HideoutJob> processActivity(job: JobContextWithProps<T>, hideoutJob: HideoutJob) {
        logger.debug("processActivity: ${hideoutJob.name}")

        @Suppress("ElseCaseInsteadOfExhaustiveWhen")
        // Springで作成されるプロキシの都合上パターンマッチングが壊れるので必須
        when (hideoutJob) {
            is ReceiveFollowJob -> {
                apReceiveFollowJobService.receiveFollowJob(
                    job.props as JobProps<ReceiveFollowJob>
                )
            }

            is DeliverPostJob -> apNoteJobService.createNoteJob(job.props as JobProps<DeliverPostJob>)
            is DeliverReactionJob -> apReactionJobService.reactionJob(job.props as JobProps<DeliverReactionJob>)
            is DeliverRemoveReactionJob -> apReactionJobService.removeReactionJob(
                job.props as JobProps<DeliverRemoveReactionJob>
            )

            else -> {
                throw IllegalStateException("WTF")
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ApJobServiceImpl::class.java)
    }
}
