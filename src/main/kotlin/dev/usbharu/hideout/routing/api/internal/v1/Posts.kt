package dev.usbharu.hideout.routing.api.internal.v1

import dev.usbharu.hideout.domain.model.hideout.form.Post
import dev.usbharu.hideout.domain.model.hideout.form.Reaction
import dev.usbharu.hideout.exception.ParameterNotExistException
import dev.usbharu.hideout.plugins.TOKEN_AUTH
import dev.usbharu.hideout.service.api.IPostApiService
import dev.usbharu.hideout.service.reaction.IReactionService
import dev.usbharu.hideout.util.InstantParseUtil
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

@Suppress("LongMethod")
fun Route.posts(postApiService: IPostApiService, reactionService: IReactionService) {
    route("/posts") {
        authenticate(TOKEN_AUTH) {
            post {
                val principal = call.principal<JWTPrincipal>() ?: throw IllegalStateException("no principal")
                val userId = principal.payload.getClaim("uid").asLong()

                val receive = call.receive<Post>()
                val create = postApiService.createPost(receive, userId)
                call.response.header("Location", create.url)
                call.respond(HttpStatusCode.OK)
            }
            route("/{id}/reactions") {
                get {
                    val principal = call.principal<JWTPrincipal>() ?: throw IllegalStateException("no principal")
                    val userId = principal.payload.getClaim("uid").asLong()
                    val postId = (
                        call.parameters["id"]?.toLong()
                            ?: throw ParameterNotExistException("Parameter(id='postsId') does not exist.")
                        )
                    call.respond(reactionService.findByPostIdForUser(postId, userId))
                }
                post {
                    val jwtPrincipal = call.principal<JWTPrincipal>() ?: throw IllegalStateException("no principal")
                    val userId = jwtPrincipal.payload.getClaim("uid").asLong()
                    val postId = call.parameters["id"]?.toLong()
                        ?: throw ParameterNotExistException("Parameter(id='postsId') does not exist.")
                    val reaction = try {
                        call.receive<Reaction>()
                    } catch (e: ContentTransformationException) {
                        Reaction(null)
                    }

                    reactionService.sendReaction(reaction.reaction ?: "❤", userId, postId)
                }
                delete {
                    val jwtPrincipal = call.principal<JWTPrincipal>() ?: throw IllegalStateException("no principal")
                    val userId = jwtPrincipal.payload.getClaim("uid").asLong()
                    val postId = call.parameters["id"]?.toLong()
                        ?: throw ParameterNotExistException("Parameter(id='postsId') does not exist.")
                    reactionService.removeReaction(userId, postId)
                }
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
                call.respond(HttpStatusCode.OK, postApiService.getAll(since, until, minId, maxId, limit, userId))
            }
            get("/{id}") {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("uid")?.asLong()
                val id = call.parameters["id"]?.toLong()
                    ?: throw ParameterNotExistException("Parameter(id='postsId') does not exist.")
                val post = postApiService.getById(id, userId)
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
                val posts = postApiService.getByUser(targetUserName, userId = userId)
                call.respond(posts)
            }
            get("/{id}") {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("uid")?.asLong()
                val id = call.parameters["id"]?.toLong()
                    ?: throw ParameterNotExistException("Parameter(name='postsId' does not exist.")
                val post = postApiService.getById(id, userId)
                call.respond(post)
            }
        }
    }
}
