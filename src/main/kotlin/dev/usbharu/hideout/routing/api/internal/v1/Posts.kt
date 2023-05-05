package dev.usbharu.hideout.routing.api.internal.v1

import dev.usbharu.hideout.domain.model.hideout.dto.PostCreateDto
import dev.usbharu.hideout.domain.model.hideout.form.Post
import dev.usbharu.hideout.service.IPostService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.posts(postService: IPostService){
    route("/posts"){
        authenticate(){
            post{
                val principal = call.principal<JWTPrincipal>() ?: throw RuntimeException("no principal")
                val username = principal.payload.getClaim("username").asString()

                val receive = call.receive<Post>()
                val postCreateDto = PostCreateDto(receive.text,username)
                postService.create(postCreateDto)
            }
        }
    }
}
