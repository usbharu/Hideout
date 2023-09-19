package dev.usbharu.hideout.routing.activitypub

import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.ActivityPubObjectResponse
import dev.usbharu.hideout.domain.model.ActivityPubStringResponse
import dev.usbharu.hideout.exception.HttpSignatureVerifyException
import dev.usbharu.hideout.service.auth.HttpSignatureVerifyService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

@Deprecated("Ktor is deprecated")
fun Routing.inbox(
    httpSignatureVerifyService: HttpSignatureVerifyService,
    apService: dev.usbharu.hideout.service.ap.APService
) {
    route("/inbox") {
        get {
            call.respond(HttpStatusCode.MethodNotAllowed)
        }
        post {
            if (httpSignatureVerifyService.verify(call.request.headers).not()) {
                throw HttpSignatureVerifyException()
            }
            val json = call.receiveText()
            call.application.log.trace("Received: $json")
            val activityTypes = apService.parseActivity(json)
            call.application.log.debug("ActivityTypes: ${activityTypes.name}")
            val response = apService.processActivity(json, activityTypes)
            when (response) {
                is ActivityPubObjectResponse -> call.respond(
                    response.httpStatusCode,
                    Config.configData.objectMapper.writeValueAsString(
                        response.message.apply {
                            context =
                                listOf("https://www.w3.org/ns/activitystreams")
                        }
                    )
                )

                is ActivityPubStringResponse -> call.respond(response.httpStatusCode, response.message)
                null -> call.respond(HttpStatusCode.NotImplemented)
            }
        }
    }
    route("/users/{name}/inbox") {
        get {
            call.respond(HttpStatusCode.MethodNotAllowed)
        }
        post {
            if (httpSignatureVerifyService.verify(call.request.headers).not()) {
                throw HttpSignatureVerifyException()
            }
            val json = call.receiveText()
            call.application.log.trace("Received: $json")
            val activityTypes = apService.parseActivity(json)
            call.application.log.debug("ActivityTypes: ${activityTypes.name}")
            val response = apService.processActivity(json, activityTypes)
            when (response) {
                is ActivityPubObjectResponse -> call.respond(
                    response.httpStatusCode,
                    Config.configData.objectMapper.writeValueAsString(
                        response.message.apply {
                            context =
                                listOf("https://www.w3.org/ns/activitystreams")
                        }
                    )
                )

                is ActivityPubStringResponse -> call.respond(response.httpStatusCode, response.message)
                null -> call.respond(HttpStatusCode.NotImplemented)
            }
        }
    }
}
