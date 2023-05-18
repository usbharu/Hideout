package dev.usbharu.hideout.routing.api.internal.v1

import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.hideout.dto.UserCreateDto
import dev.usbharu.hideout.domain.model.hideout.form.UserCreate
import dev.usbharu.hideout.exception.ParameterNotExistException
import dev.usbharu.hideout.plugins.TOKEN_AUTH
import dev.usbharu.hideout.service.impl.IUserService
import dev.usbharu.hideout.util.AcctUtil
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.users(userService: IUserService) {
    route("/users") {
        get {
            call.respond(userService.findAll())
        }
        post {
            val userCreate = call.receive<UserCreate>()
            if (userService.usernameAlreadyUse(userCreate.username)) {
                return@post call.respond(HttpStatusCode.BadRequest)
            }
            val user = userService.createLocalUser(
                UserCreateDto(
                    userCreate.username,
                    userCreate.username,
                    "",
                    userCreate.password
                )
            )
            call.response.header("Location", "${Config.configData.url}/api/internal/v1/users/${user.name}")
            call.respond(HttpStatusCode.OK)
        }
        route("/{name}") {

            route("/followers") {
                get {
                    val userParameter = call.parameters["name"]
                        ?: throw ParameterNotExistException("Parameter(name='userName@domain') does not exist.")
                    if (userParameter.toLongOrNull() != null) {
                        return@get call.respond(userService.findFollowersById(userParameter.toLong()))
                    }
                    val acct = AcctUtil.parse(userParameter)
                    return@get call.respond(userService.findFollowersByNameAndDomain(acct.username, acct.domain))
                }
                authenticate(TOKEN_AUTH) {

                    post {
                        val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("uid")?.asLong()
                            ?: throw IllegalStateException("no principal")
                        val userParameter = call.parameters["name"]
                            ?: throw ParameterNotExistException("Parameter(name='userName@domain') does not exist.")
                        if (userParameter.toLongOrNull() != null) {
                            if (userService.addFollowers(userParameter.toLong(), userId)) {
                                return@post call.respond(HttpStatusCode.OK)
                            } else {
                                return@post call.respond(HttpStatusCode.Accepted)
                            }
                        }
                        val acct = AcctUtil.parse(userParameter)
                        val targetUser = userService.findByNameAndDomain(acct.username, acct.domain)
                        if (userService.addFollowers(targetUser.id, userId)) {
                            return@post call.respond(HttpStatusCode.OK)
                        } else {
                            return@post call.respond(HttpStatusCode.Accepted)
                        }
                    }
                }
            }
            route("/following") {
                get {
                    val userParameter = (call.parameters["name"]
                        ?: throw ParameterNotExistException("Parameter(name='userName@domain') does not exist."))
                    if (userParameter.toLongOrNull() != null) {
                        return@get call.respond(userService.findFollowingById(userParameter.toLong()))
                    }
                    val acct = AcctUtil.parse(userParameter)
                    return@get call.respond(userService.findFollowingByNameAndDomain(acct.username, acct.domain))
                }
            }
        }

    }
}
