package dev.usbharu.hideout.plugins

import dev.usbharu.hideout.routing.activitypub.inbox
import dev.usbharu.hideout.routing.activitypub.outbox
import dev.usbharu.hideout.routing.activitypub.usersAP
import dev.usbharu.hideout.routing.wellknown.webfinger
import dev.usbharu.hideout.service.activitypub.ActivityPubService
import dev.usbharu.hideout.service.signature.HttpSignatureVerifyService
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    httpSignatureVerifyService: HttpSignatureVerifyService,
    activityPubService: ActivityPubService
) {
    install(AutoHeadResponse)
    routing {
        inbox(httpSignatureVerifyService, activityPubService)
        outbox()
        usersAP(activityPubService)
        webfinger()
    }
}
