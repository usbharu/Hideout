package dev.usbharu.hideout.service

import com.fasterxml.jackson.databind.ObjectMapper

class ActivityPubService(private val objectMapper: ObjectMapper) {

    enum class ActivityType{
        Follow,
        Undo
    }

    fun switchApType(json:String):ActivityType{
        val typeAsText = objectMapper.readTree(json).get("type").asText()
        return when(typeAsText){
            "Follow" -> ActivityType.Follow
            "Undo" -> ActivityType.Undo
            else -> throw IllegalArgumentException()
        }
    }
}
