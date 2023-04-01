package dev.usbharu.hideout.routing.wellknown

import io.ktor.server.routing.*

fun Routing.webfinger(){
    route("/.well-known/webfinger"){
        get {

        }
    }
}
