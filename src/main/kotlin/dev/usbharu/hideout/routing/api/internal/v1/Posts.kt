package dev.usbharu.hideout.routing.api.internal.v1

import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.hideout.dto.PostCreateDto
import dev.usbharu.hideout.domain.model.hideout.form.Post
import dev.usbharu.hideout.exception.ParameterNotExistException
import dev.usbharu.hideout.exception.PostNotFoundException
import dev.usbharu.hideout.plugins.TOKEN_AUTH
import dev.usbharu.hideout.service.IPostService
import dev.usbharu.hideout.util.AcctUtil
import dev.usbharu.hideout.util.InstantParseUtil
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

@Suppress("LongMethod")
fun Route.posts(postService: IPostService) {
    route("/posts") {
        authenticate(TOKEN_AUTH) {
            post {
                val principal = call.principal<JWTPrincipal>() ?: throw IllegalStateException("no principal")
                val userId = principal.payload.getClaim("uid").asLong()

                val receive = call.receive<Post>()
                val postCreateDto = PostCreateDto(
                    text = receive.text,
                    overview = receive.overview,
                    visibility = receive.visibility,
                    repostId = receive.repostId,
                    repolyId = receive.replyId,
                    userId = userId
                )
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
                call.respond(HttpStatusCode.OK, postService.findAll(since, until, minId, maxId, limit, userId))
            }
            get("/{id}") {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("uid")?.asLong()
                val id = call.parameters["id"]?.toLong()
                    ?: throw ParameterNotExistException("Parameter(id='postsId') does not exist.")
                val post = (
                        postService.findByIdForUser(id, userId)
                            ?: throw PostNotFoundException("$id was not found or is not authorized.")
                        )
                call.respond(post)
            }
        }
    }
    route("/users/{name}/posts") {
        authenticate(TOKEN_AUTH, optional = true) {
            get {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("uid")?.asLong()
                val targetUserName = call.parameters["name"]
                    ?: throw ParameterNotExistException("Parameter(name='userName@domain') does not exist.")
                val targetUserId = targetUserName.toLongOrNull()
                val posts = if (targetUserId == null) {
                    val acct = AcctUtil.parse(targetUserName)
                    postService.findByUserNameAndDomainForUser(
                        acct.username,
                        acct.domain ?: Config.configData.domain,
                        forUserId = userId
                    )
                } else {
                    postService.findByUserIdForUser(targetUserId, forUserId = userId)
                }
                call.respond(posts)
            }
            get("/{id}") {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("uid")?.asLong()
                val id = call.parameters["id"]?.toLong()
                    ?: throw ParameterNotExistException("Parameter(name='postsId' does not exist.")
                val post = (
                        postService.findByIdForUser(id, userId)
                            ?: throw PostNotFoundException("$id was not found or is not authorized.")
                        )
                call.respond(post)
            }
        }
    }
}
