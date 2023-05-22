package dev.usbharu.hideout.routing.api.internal.v1

import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.hideout.dto.UserCreateDto
import dev.usbharu.hideout.domain.model.hideout.form.UserCreate
import dev.usbharu.hideout.exception.ParameterNotExistException
import dev.usbharu.hideout.plugins.TOKEN_AUTH
import dev.usbharu.hideout.service.IUserApiService
import dev.usbharu.hideout.service.impl.IUserService
import dev.usbharu.hideout.util.AcctUtil
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

@Suppress("LongMethod")
fun Route.users(userService: IUserService, userApiService: IUserApiService) {
    route("/users") {
        get {
            call.respond(userApiService.findAll())
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
            call.respond(HttpStatusCode.Created)
        }
        route("/{name}") {
            authenticate(TOKEN_AUTH, optional = true) {
                get {
                    val userParameter = (
                            call.parameters["name"]
                                ?: throw ParameterNotExistException("Parameter(name='userName@domain') does not exist.")
                            )
                    if (userParameter.toLongOrNull() != null) {
                        return@get call.respond(userApiService.findById(userParameter.toLong()))
                    } else {
                        val acct = AcctUtil.parse(userParameter)
                        return@get call.respond(userApiService.findByAcct(acct))
                    }
                }
            }

            route("/followers") {
                get {
                    val userParameter = call.parameters["name"]
                        ?: throw ParameterNotExistException("Parameter(name='userName@domain') does not exist.")
                    if (userParameter.toLongOrNull() != null) {
                        return@get call.respond(userApiService.findFollowers(userParameter.toLong()))
                    }
                    val acct = AcctUtil.parse(userParameter)
                    return@get call.respond(userApiService.findFollowersByAcct(acct))
                }
                authenticate(TOKEN_AUTH) {
                    post {
                        val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("uid")?.asLong()
                            ?: throw IllegalStateException("no principal")
                        val userParameter = call.parameters["name"]
                            ?: throw ParameterNotExistException("Parameter(name='userName@domain') does not exist.")
                        if (userParameter.toLongOrNull() != null) {
                            if (userService.followRequest(userParameter.toLong(), userId)) {
                                return@post call.respond(HttpStatusCode.OK)
                            } else {
                                return@post call.respond(HttpStatusCode.Accepted)
                            }
                        }
                        val acct = AcctUtil.parse(userParameter)
                        val targetUser = userApiService.findByAcct(acct)
                        if (userService.followRequest(targetUser.id, userId)) {
                            return@post call.respond(HttpStatusCode.OK)
                        } else {
                            return@post call.respond(HttpStatusCode.Accepted)
                        }
                    }
                }
            }
            route("/following") {
                get {
                    val userParameter = (
                            call.parameters["name"]
                                ?: throw ParameterNotExistException("Parameter(name='userName@domain') does not exist.")
                            )
                    if (userParameter.toLongOrNull() != null) {
                        return@get call.respond(userApiService.findFollowings(userParameter.toLong()))
                    }
                    val acct = AcctUtil.parse(userParameter)
                    return@get call.respond(userApiService.findFollowingsByAcct(acct))
                }
            }
        }
    }
}
