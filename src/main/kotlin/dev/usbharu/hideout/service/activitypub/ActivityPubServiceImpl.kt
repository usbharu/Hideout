package dev.usbharu.hideout.service.activitypub

import com.fasterxml.jackson.databind.JsonNode
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.exception.JsonParseException

class ActivityPubServiceImpl : ActivityPubService {
    override fun parseActivity(json: String): ActivityType {
        val readTree = Config.configData.objectMapper.readTree(json)
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

    override fun processActivity(json: String, type: ActivityType) {
        when (type) {
            ActivityType.Accept -> TODO()
            ActivityType.Add -> TODO()
            ActivityType.Announce -> TODO()
            ActivityType.Arrive -> TODO()
            ActivityType.Block -> TODO()
            ActivityType.Create -> TODO()
            ActivityType.Delete -> TODO()
            ActivityType.Dislike -> TODO()
            ActivityType.Flag -> TODO()
            ActivityType.Follow -> TODO()
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
            ActivityType.Undo -> TODO()
            ActivityType.Update -> TODO()
            ActivityType.View -> TODO()
            ActivityType.Other -> TODO()
        }
    }
}
