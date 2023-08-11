package dev.usbharu.hideout.plugins

import dev.usbharu.hideout.query.FollowerQueryService
import dev.usbharu.hideout.query.UserQueryService
import dev.usbharu.hideout.routing.activitypub.inbox
import dev.usbharu.hideout.routing.activitypub.outbox
import dev.usbharu.hideout.routing.activitypub.usersAP
import dev.usbharu.hideout.routing.api.internal.v1.auth
import dev.usbharu.hideout.routing.api.internal.v1.posts
import dev.usbharu.hideout.routing.api.internal.v1.users
import dev.usbharu.hideout.routing.wellknown.webfinger
import dev.usbharu.hideout.service.activitypub.ActivityPubService
import dev.usbharu.hideout.service.activitypub.ActivityPubUserService
import dev.usbharu.hideout.service.api.IPostApiService
import dev.usbharu.hideout.service.api.IUserApiService
import dev.usbharu.hideout.service.api.UserAuthApiService
import dev.usbharu.hideout.service.auth.HttpSignatureVerifyService
import dev.usbharu.hideout.service.reaction.IReactionService
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
    postService: IPostApiService,
    userApiService: IUserApiService,
    reactionService: IReactionService,
    userQueryService: UserQueryService,
    followerQueryService: FollowerQueryService,
    userAuthApiService: UserAuthApiService
) {
    install(AutoHeadResponse)
    routing {
        inbox(httpSignatureVerifyService, activityPubService)
        outbox()
        usersAP(activityPubUserService, userQueryService, followerQueryService)
        webfinger(userQueryService)
        route("/api/internal/v1") {
            posts(postService)
            users(userService, userApiService)
            auth(userAuthApiService)
        }
    }
}
