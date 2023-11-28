package dev.usbharu.hideout.activitypub.service.activity.follow

import com.fasterxml.jackson.databind.ObjectMapper
import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.service.common.AbstractActivityPubProcessor
import dev.usbharu.hideout.activitypub.service.common.ActivityPubProcessContext
import dev.usbharu.hideout.activitypub.service.common.ActivityType
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.external.job.ReceiveFollowJob
import dev.usbharu.hideout.core.external.job.ReceiveFollowJobParam
import dev.usbharu.hideout.core.service.job.JobQueueParentService

class APFollowProcessor(
    transaction: Transaction,
    private val jobQueueParentService: JobQueueParentService,
    private val objectMapper: ObjectMapper
) :
    AbstractActivityPubProcessor<Follow>(transaction) {
    override suspend fun internalProcess(activity: ActivityPubProcessContext<Follow>) {
        logger.info("FOLLOW from: {} to {}", activity.activity.actor, activity.activity.`object`)

        // inboxをジョブキューに乗せているので既に不要だが、フォロー承認制アカウントを実装する際に必要なので残す
        val jobProps = ReceiveFollowJobParam(
            activity.activity.actor,
            objectMapper.writeValueAsString(activity.activity),
            activity.activity.`object`
        )
        jobQueueParentService.scheduleTypeSafe(ReceiveFollowJob, jobProps)
    }

    override fun isSupported(activityType: ActivityType): Boolean = activityType == ActivityType.Follow

    override fun type(): Class<Follow> = Follow::class.java
}
