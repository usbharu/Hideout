package dev.usbharu.hideout.routing.api.internal.v1

import dev.usbharu.hideout.domain.model.hideout.dto.PostCreateDto
import dev.usbharu.hideout.domain.model.hideout.form.Post
import dev.usbharu.hideout.plugins.TOKEN_AUTH
import dev.usbharu.hideout.service.IPostService
import dev.usbharu.hideout.util.InstantParseUtil
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.posts(postService: IPostService) {
    route("/posts") {
        authenticate(TOKEN_AUTH) {
            post {
                val principal = call.principal<JWTPrincipal>() ?: throw RuntimeException("no principal")
                val userId = principal.payload.getClaim("uid").asLong()

                val receive = call.receive<Post>()
                val postCreateDto = PostCreateDto(receive.text, userId)
                val create = postService.create(postCreateDto)
                call.response.header("Location", create.url)
                call.respond(HttpStatusCode.OK)
            }
        }
        authenticate(TOKEN_AUTH, optional = true) {
            get {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("uid")?.asLong()
                val since = InstantParseUtil.parse(call.request.queryParameters["since"])
                val until = InstantParseUtil.parse(call.request.queryParameters["until"])
                val minId = call.request.queryParameters["minId"]?.toLong()
                val maxId = call.request.queryParameters["maxId"]?.toLong()
                val limit = call.request.queryParameters["limit"]?.toInt()
                postService.findAll(since, until, minId, maxId, limit, userId)
            }
        }
    }
}
