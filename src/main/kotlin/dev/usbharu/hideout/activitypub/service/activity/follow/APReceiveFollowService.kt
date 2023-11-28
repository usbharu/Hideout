package dev.usbharu.hideout.activitypub.service.activity.follow

import com.fasterxml.jackson.databind.ObjectMapper
import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.core.external.job.ReceiveFollowJob
import dev.usbharu.hideout.core.service.job.JobQueueParentService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

interface APReceiveFollowService {
    suspend fun receiveFollow(follow: Follow)
}

@Service
class APReceiveFollowServiceImpl(
    private val jobQueueParentService: JobQueueParentService,
    @Qualifier("activitypub") private val objectMapper: ObjectMapper
) : APReceiveFollowService {
    override suspend fun receiveFollow(follow: Follow) {
        logger.info("FOLLOW from: {} to: {}", follow.actor, follow.apObject)
        jobQueueParentService.schedule(ReceiveFollowJob) {
            props[ReceiveFollowJob.actor] = follow.actor
            props[ReceiveFollowJob.follow] = objectMapper.writeValueAsString(follow)
            props[ReceiveFollowJob.targetActor] = follow.apObject
        }
        return
    }

    companion object {
        private val logger = LoggerFactory.getLogger(APReceiveFollowServiceImpl::class.java)
    }
}
