package dev.usbharu.hideout.service.activitypub

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.config.Config.configData
import dev.usbharu.hideout.domain.model.ActivityPubResponse
import dev.usbharu.hideout.domain.model.ap.Follow
import dev.usbharu.hideout.domain.model.job.DeliverPostJob
import dev.usbharu.hideout.domain.model.job.HideoutJob
import dev.usbharu.hideout.domain.model.job.ReceiveFollowJob
import dev.usbharu.hideout.exception.JsonParseException
import kjob.core.dsl.JobContextWithProps
import kjob.core.job.JobProps
import org.koin.core.annotation.Single
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Single
class ActivityPubServiceImpl(
    private val activityPubReceiveFollowService: ActivityPubReceiveFollowService,
    private val activityPubNoteService: ActivityPubNoteService,
    private val activityPubUndoService: ActivityPubUndoService,
    private val activityPubAcceptService: ActivityPubAcceptService
) : ActivityPubService {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)
    override fun parseActivity(json: String): ActivityType {
        val readTree = configData.objectMapper.readTree(json)
        logger.debug("readTree: {}", readTree)
        if (readTree.isObject.not()) {
            throw JsonParseException("Json is not object.")
        }
        val type = readTree["type"]
        if (type.isArray) {
            return type.mapNotNull { jsonNode: JsonNode ->
                ActivityType.values().firstOrNull { it.name.equals(jsonNode.asText(), true) }
            }.first()
        }
        return ActivityType.values().first { it.name.equals(type.asText(), true) }
    }

    @Suppress("CyclomaticComplexMethod", "NotImplementedDeclaration")
    override suspend fun processActivity(json: String, type: ActivityType): ActivityPubResponse {
        return when (type) {
            ActivityType.Accept -> activityPubAcceptService.receiveAccept(configData.objectMapper.readValue(json))
            ActivityType.Add -> TODO()
            ActivityType.Announce -> TODO()
            ActivityType.Arrive -> TODO()
            ActivityType.Block -> TODO()
            ActivityType.Create -> TODO()
            ActivityType.Delete -> TODO()
            ActivityType.Dislike -> TODO()
            ActivityType.Flag -> TODO()
            ActivityType.Follow -> activityPubReceiveFollowService.receiveFollow(
                configData.objectMapper.readValue(
                    json,
                    Follow::class.java
                )
            )

            ActivityType.Ignore -> TODO()
            ActivityType.Invite -> TODO()
            ActivityType.Join -> TODO()
            ActivityType.Leave -> TODO()
            ActivityType.Like -> TODO()
            ActivityType.Listen -> TODO()
            ActivityType.Move -> TODO()
            ActivityType.Offer -> TODO()
            ActivityType.Question -> TODO()
            ActivityType.Reject -> TODO()
            ActivityType.Read -> TODO()
            ActivityType.Remove -> TODO()
            ActivityType.TentativeReject -> TODO()
            ActivityType.TentativeAccept -> TODO()
            ActivityType.Travel -> TODO()
            ActivityType.Undo -> activityPubUndoService.receiveUndo(configData.objectMapper.readValue(json))
            ActivityType.Update -> TODO()
            ActivityType.View -> TODO()
            ActivityType.Other -> TODO()
        }
    }

    override suspend fun <T : HideoutJob> processActivity(job: JobContextWithProps<T>, hideoutJob: HideoutJob) {
        logger.debug("processActivity: ${hideoutJob.name}")
        when (hideoutJob) {
            ReceiveFollowJob -> activityPubReceiveFollowService.receiveFollowJob(job.props as JobProps<ReceiveFollowJob>)
            DeliverPostJob -> activityPubNoteService.createNoteJob(job.props as JobProps<DeliverPostJob>)
        }
    }
}
