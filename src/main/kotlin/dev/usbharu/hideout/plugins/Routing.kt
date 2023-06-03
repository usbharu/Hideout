package dev.usbharu.hideout.plugins

import dev.usbharu.hideout.routing.activitypub.inbox
import dev.usbharu.hideout.routing.activitypub.outbox
import dev.usbharu.hideout.routing.activitypub.usersAP
import dev.usbharu.hideout.routing.api.internal.v1.posts
import dev.usbharu.hideout.routing.api.internal.v1.users
import dev.usbharu.hideout.routing.api.mastodon.v1.statuses
import dev.usbharu.hideout.routing.wellknown.webfinger
import dev.usbharu.hideout.service.activitypub.ActivityPubService
import dev.usbharu.hideout.service.activitypub.ActivityPubUserService
import dev.usbharu.hideout.service.api.IUserApiService
import dev.usbharu.hideout.service.auth.HttpSignatureVerifyService
import dev.usbharu.hideout.service.post.IPostService
import dev.usbharu.hideout.service.user.IUserService
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.routing.*

@Suppress("LongParameterList")
fun Application.configureRouting(
    httpSignatureVerifyService: HttpSignatureVerifyService,
    activityPubService: ActivityPubService,
    userService: IUserService,
    activityPubUserService: ActivityPubUserService,
    postService: IPostService,
    userApiService: IUserApiService
) {
    install(AutoHeadResponse)
    routing {
        inbox(httpSignatureVerifyService, activityPubService)
        outbox()
        usersAP(activityPubUserService, userService)
        webfinger(userService)

        route("/api/v1") {
            statuses(postService)
        }
        route("/api/internal/v1") {
            posts(postService)
            users(userService, userApiService)
        }
    }
}
