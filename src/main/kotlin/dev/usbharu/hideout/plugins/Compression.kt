package dev.usbharu.hideout.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*

fun Application.configureCompression() {
    install(Compression) {
        gzip {
            matchContentType(ContentType.Application.JavaScript)
            priority = 1.0
        }
        deflate {
            matchContentType(ContentType.Application.JavaScript)
            priority = 10.0
            minimumSize(1024) // condition
        }
    }
}
