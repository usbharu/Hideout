package dev.usbharu.hideout.routing

import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.userActivityPubRouting(){
    routing {
        route("/users/{name}") {
            route("/inbox"){
                get {

                }
                post {

                }
            }
            route("/outbox") {
                get {

                }
                post {

                }
            }
        }
    }
}
