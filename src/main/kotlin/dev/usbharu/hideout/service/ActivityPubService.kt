package dev.usbharu.hideout.service

import com.fasterxml.jackson.databind.ObjectMapper
import dev.usbharu.hideout.config.Config

class ActivityPubService() {

    enum class ActivityType{
        Follow,
        Undo
    }

    fun switchApType(json:String):ActivityType{
        val typeAsText = Config.configData.objectMapper.readTree(json).get("type").asText()
        return when(typeAsText){
            "Follow" -> ActivityType.Follow
            "Undo" -> ActivityType.Undo
            else -> throw IllegalArgumentException()
        }
    }
}
