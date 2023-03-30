package dev.usbharu.hideout.routing

import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.ap.Follow
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.service.ActivityPubService
import dev.usbharu.hideout.service.ActivityPubUserService
import dev.usbharu.hideout.util.HttpUtil
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.userActivityPubRouting(activityPubService: ActivityPubService, activityPubUserService: ActivityPubUserService) {
    routing {
        route("/users/{name}") {
            route("/inbox") {
                get {
                    call.respond(HttpStatusCode.MethodNotAllowed)
                }
                post {
                    if (!HttpUtil.isContentTypeOfActivityPub(call.request.contentType())) {
                        return@post call.respond(HttpStatusCode.BadRequest)
                    }
                    val bodyText = call.receiveText()
                    println(bodyText)
                    call.respond(HttpStatusCode.OK)
                    when (activityPubService.switchApType(bodyText)) {
                        ActivityPubService.ActivityType.Follow -> {
                            val readValue = Config.configData.objectMapper.readValue<Follow>(bodyText)
                            activityPubUserService.receiveFollow(readValue)
                            return@post
                        }

                        ActivityPubService.ActivityType.Undo -> {
                            TODO()
                        }
                    }

                }
            }
            route("/outbox") {
                get {
                    call.respond(HttpStatusCode.MethodNotAllowed)

                }
                post {

                    call.respond(HttpStatusCode.MethodNotAllowed)
                }
            }
        }
    }
}
