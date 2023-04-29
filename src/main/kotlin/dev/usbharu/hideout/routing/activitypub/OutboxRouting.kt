package dev.usbharu.hideout.routing.activitypub

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.outbox() {
    route("/outbox") {
        get {
            call.respond(HttpStatusCode.NotImplemented)
        }
        post {
            call.respond(HttpStatusCode.NotImplemented)
        }
    }
    route("/users/{name}/outbox") {
        get {
            call.respond(HttpStatusCode.NotImplemented)
        }
        post {
            call.respond(HttpStatusCode.NotImplemented)
        }
    }
}
