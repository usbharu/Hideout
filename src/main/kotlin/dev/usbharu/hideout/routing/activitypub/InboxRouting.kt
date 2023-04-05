package dev.usbharu.hideout.routing.activitypub

import dev.usbharu.hideout.exception.HttpSignatureVerifyException
import dev.usbharu.hideout.service.signature.HttpSignatureVerifyService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.inbox(httpSignatureVerifyService: HttpSignatureVerifyService,activityPubService: dev.usbharu.hideout.service.activitypub.ActivityPubService){

        route("/inbox") {
            get {
                call.respond(HttpStatusCode.MethodNotAllowed)
            }
            post {
                if (httpSignatureVerifyService.verify(call.request.headers).not()) {
                    throw HttpSignatureVerifyException()
                }
                val json = call.receiveText()
                val activityTypes = activityPubService.parseActivity(json)
                activityPubService.processActivity(json,activityTypes)
                call.respond(HttpStatusCode.NotImplemented)
            }
        }
        route("/users/{name}/inbox"){
            get {
                call.respond(HttpStatusCode.NotImplemented)
            }
            post {
                call.respond(HttpStatusCode.MethodNotAllowed)
            }
        }

}
