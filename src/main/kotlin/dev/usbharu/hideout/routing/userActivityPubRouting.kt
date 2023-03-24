package dev.usbharu.hideout.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.userActivityPubRouting() {
    routing {
        route("/users/{name}") {
            route("/inbox") {
                get {
                    call.respond(HttpStatusCode.OK)
                }
                post {
                    call.respond(HttpStatusCode.OK)

                }
            }
            route("/outbox") {
                get {
                    call.respond(HttpStatusCode.OK)

                }
                post {

                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}
