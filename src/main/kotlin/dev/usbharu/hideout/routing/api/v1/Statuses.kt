package dev.usbharu.hideout.routing.api.v1

import dev.usbharu.hideout.domain.model.Post
import dev.usbharu.hideout.domain.model.api.StatusForPost
import dev.usbharu.hideout.service.IPostService
import dev.usbharu.hideout.service.impl.PostService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.statuses(postService: IPostService) {
    route("/statuses") {
        post {
            val status: StatusForPost = call.receive()
            val post = Post(
                userId = status.userId,
                createdAt = System.currentTimeMillis(),
                text = status.status,
                visibility = 1
            )
            postService.create(post)
            call.respond(status)
        }
    }
}
