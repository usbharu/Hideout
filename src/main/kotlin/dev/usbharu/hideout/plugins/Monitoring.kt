package dev.usbharu.hideout.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import org.slf4j.event.Level

@Deprecated("Ktor is deprecated")
fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
    }
}
