package dev.usbharu.hideout.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureStaticRouting() {
    routing {
        get("/") {
            call.respondText(
                String.javaClass.classLoader.getResourceAsStream("static/index.html").readAllBytes().decodeToString(),
                contentType = ContentType.Text.Html
            )
        }
        static("/") {
            resources("static")
        }
    }
}
