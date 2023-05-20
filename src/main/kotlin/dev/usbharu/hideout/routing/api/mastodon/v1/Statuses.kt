package dev.usbharu.hideout.routing.api.mastodon.v1

import dev.usbharu.hideout.service.IPostService
import io.ktor.server.routing.*

@Suppress("UnusedPrivateMember")
fun Route.statuses(postService: IPostService) {
//    route("/statuses") {
//        post {
//            val status: StatusForPost = call.receive()
//            val post = dev.usbharu.hideout.domain.model.hideout.form.Post(
//                userId = status.userId,
//                createdAt = System.currentTimeMillis(),
//                text = status.status,
//                visibility = 1
//            )
//            postService.create(post)
//            call.respond(status)
//        }
//    }
}
