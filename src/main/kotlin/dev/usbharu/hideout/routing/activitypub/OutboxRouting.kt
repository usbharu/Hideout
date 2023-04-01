package dev.usbharu.hideout.routing.activitypub

import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Routing.outbox() {

        route("/outbox") {
            get {

            }
            post {

            }
        }
        route("/users/{name}/outbox"){
            get {

            }
            post {

            }
        }

}
