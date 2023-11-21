package dev.usbharu.hideout.activitypub.service.common

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.service.activity.accept.APAcceptServiceImpl
import dev.usbharu.hideout.activitypub.service.activity.create.APCreateServiceImpl
import dev.usbharu.hideout.activitypub.service.activity.delete.APReceiveDeleteServiceImpl
import dev.usbharu.hideout.activitypub.service.activity.follow.APReceiveFollowJobService
import dev.usbharu.hideout.activitypub.service.activity.follow.APReceiveFollowServiceImpl
import dev.usbharu.hideout.activitypub.service.activity.like.APLikeServiceImpl
import dev.usbharu.hideout.activitypub.service.activity.like.ApReactionJobService
import dev.usbharu.hideout.activitypub.service.activity.undo.APUndoServiceImpl
import dev.usbharu.hideout.activitypub.service.objects.note.ApNoteJobService
import dev.usbharu.hideout.core.external.job.*

import kjob.core.dsl.JobContextWithProps
import kjob.core.job.JobProps
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class ApJobServiceImpl(
    private val apReceiveFollowJobService: APReceiveFollowJobService,
    private val apNoteJobService: ApNoteJobService,
    private val apReactionJobService: ApReactionJobService,
    private val APAcceptServiceImpl: APAcceptServiceImpl,
    private val APReceiveFollowServiceImpl: APReceiveFollowServiceImpl,
    private val APCreateServiceImpl: APCreateServiceImpl,
    private val APLikeServiceImpl: APLikeServiceImpl,
    private val APUndoServiceImpl: APUndoServiceImpl,
    private val APReceiveDeleteServiceImpl: APReceiveDeleteServiceImpl,
    @Qualifier("activitypub") private val objectMapper: ObjectMapper
) : ApJobService {
    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    override suspend fun <T : HideoutJob> processActivity(job: JobContextWithProps<T>, hideoutJob: HideoutJob) {
        logger.debug("processActivity: ${hideoutJob.name}")

        @Suppress("ElseCaseInsteadOfExhaustiveWhen")
        // Springで作成されるプロキシの都合上パターンマッチングが壊れるので必須
        when (hideoutJob) {
            is InboxJob -> {
                val typeString = (job.props as JobProps<InboxJob>)[InboxJob.type]
                val json = (job.props as JobProps<InboxJob>)[InboxJob.json]
                val type = ActivityType.valueOf(typeString)
                when (type) {
                    ActivityType.Accept -> APAcceptServiceImpl.receiveAccept(objectMapper.readValue(json))
                    ActivityType.Follow ->
                        APReceiveFollowServiceImpl
                            .receiveFollow(objectMapper.readValue(json, Follow::class.java))

                    ActivityType.Create -> APCreateServiceImpl.receiveCreate(objectMapper.readValue(json))
                    ActivityType.Like -> APLikeServiceImpl.receiveLike(objectMapper.readValue(json))
                    ActivityType.Undo -> APUndoServiceImpl.receiveUndo(objectMapper.readValue(json))
                    ActivityType.Delete -> APReceiveDeleteServiceImpl.receiveDelete(objectMapper.readValue(json))

                    else -> {
                        throw IllegalArgumentException("$type is not supported.")
                    }
                }
            }
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
