package dev.usbharu.hideout.plugins

import dev.usbharu.hideout.ap.JsonLd
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.util.HttpUtil.Activity
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

suspend fun <T : JsonLd> ApplicationCall.respondAp(message: T, status: HttpStatusCode = HttpStatusCode.OK) {
    message.context += "https://www.w3.org/activitystreams"
    val activityJson = Config.configData.objectMapper.writeValueAsString(message)
    respondText(activityJson, ContentType.Application.Activity, status)
}
