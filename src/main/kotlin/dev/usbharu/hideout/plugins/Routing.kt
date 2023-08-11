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
import dev.usbharu.hideout.service.ap.APService
import dev.usbharu.hideout.service.ap.APUserService
import dev.usbharu.hideout.service.api.IPostApiService
import dev.usbharu.hideout.service.api.IUserApiService
import dev.usbharu.hideout.service.api.UserAuthApiService
import dev.usbharu.hideout.service.api.WebFingerApiService
import dev.usbharu.hideout.service.auth.HttpSignatureVerifyService
import dev.usbharu.hideout.service.core.Transaction
import dev.usbharu.hideout.service.user.IUserService
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.routing.*

@Suppress("LongParameterList")
fun Application.configureRouting(
    httpSignatureVerifyService: HttpSignatureVerifyService,
    apService: APService,
    userService: IUserService,
    apUserService: APUserService,
    postService: IPostApiService,
    userApiService: IUserApiService,
    userQueryService: UserQueryService,
    followerQueryService: FollowerQueryService,
    userAuthApiService: UserAuthApiService,
    webFingerApiService: WebFingerApiService,
    transaction: Transaction
) {
    install(AutoHeadResponse)
    routing {
        inbox(httpSignatureVerifyService, apService)
        outbox()
        usersAP(apUserService, userQueryService, followerQueryService, transaction)
        webfinger(webFingerApiService)
        route("/api/internal/v1") {
            posts(postService)
            users(userService, userApiService)
            auth(userAuthApiService)
        }
    }
}
