package dev.usbharu.hideout.routing

import dev.usbharu.hideout.service.ActivityPubService
import dev.usbharu.hideout.util.HttpUtil
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.userActivityPubRouting(activityPubService: ActivityPubService) {
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
                    activityPubService.switchApType(bodyText)

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
