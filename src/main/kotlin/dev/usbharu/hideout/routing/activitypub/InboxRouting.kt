package dev.usbharu.hideout.routing.activitypub

import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Routing.inbox(){

        route("/inbox") {
            get {

            }
            post {

            }
        }
        route("/users/{name}/inbox"){
            get {

            }
            post {

            }
        }

}
