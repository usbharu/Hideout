package dev.usbharu.hideout.service.ap

import com.fasterxml.jackson.databind.ObjectMapper
import dev.usbharu.hideout.domain.model.ActivityPubResponse
import dev.usbharu.hideout.domain.model.ActivityPubStringResponse
import dev.usbharu.hideout.domain.model.ap.Follow
import dev.usbharu.hideout.domain.model.job.ReceiveFollowJob
import dev.usbharu.hideout.service.job.JobQueueParentService
import io.ktor.http.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

interface APReceiveFollowService {
    suspend fun receiveFollow(follow: Follow): ActivityPubResponse
}

@Service
class APReceiveFollowServiceImpl(
    private val jobQueueParentService: JobQueueParentService,
    @Qualifier("activitypub") private val objectMapper: ObjectMapper
) : APReceiveFollowService {
    override suspend fun receiveFollow(follow: Follow): ActivityPubResponse {
        logger.info("FOLLOW from: {} to: {}", follow.actor, follow.`object`)
        jobQueueParentService.schedule(ReceiveFollowJob) {
            props[ReceiveFollowJob.actor] = follow.actor
            props[ReceiveFollowJob.follow] = objectMapper.writeValueAsString(follow)
            props[ReceiveFollowJob.targetActor] = follow.`object`
        }
        return ActivityPubStringResponse(HttpStatusCode.OK, "{}", ContentType.Application.Json)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(APReceiveFollowServiceImpl::class.java)
    }
}
