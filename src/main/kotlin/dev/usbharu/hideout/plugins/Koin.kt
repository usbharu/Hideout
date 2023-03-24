package dev.usbharu.hideout.plugins

import io.ktor.server.application.*
import org.koin.core.module.Module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin(module: Module) {
    install(Koin) {
        slf4jLogger()
        modules(module)
    }
}
