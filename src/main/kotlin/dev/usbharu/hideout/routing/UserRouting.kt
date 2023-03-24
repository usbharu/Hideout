package dev.usbharu.hideout.routing

import dev.usbharu.hideout.domain.model.User
import dev.usbharu.hideout.plugins.UserSession
import dev.usbharu.hideout.plugins.respondAp
import dev.usbharu.hideout.plugins.tokenAuth
import dev.usbharu.hideout.service.ActivityPubUserService
import dev.usbharu.hideout.service.UserService
import dev.usbharu.hideout.util.HttpUtil
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

@Suppress("unused")
fun Application.user(userService: UserService,activityPubUserService: ActivityPubUserService) {
    routing {
        route("/users") {
            authenticate( tokenAuth,optional = true) {

                get {
                    val limit = call.request.queryParameters["limit"]?.toInt()
                    val offset = call.request.queryParameters["offset"]?.toLong()
                    val result = userService.findAll(limit, offset)
                    call.respond(result)
                }
                post {
                    val user = call.receive<User>()
                    userService.create(user)
                    call.response.header(
                        HttpHeaders.Location,
                        call.request.path() + "/${user.name}"
                    )
                    call.respond(HttpStatusCode.Created)
                }
                get("/{name}") {
                    val contentType = ContentType.parse(call.request.accept()?:"*/*")
                    val typeOfActivityPub = HttpUtil.isContentTypeOfActivityPub(
                        contentType.contentType,
                        contentType.contentSubtype,
                        contentType.parameter("profile").orEmpty()
                    )
                    val name = call.parameters["name"]
                    if (typeOfActivityPub) {
                        val userModel = activityPubUserService.generateUserModel(name!!)
                        return@get call.respondAp(userModel)
                    }

                    val principal = call.principal<UserIdPrincipal>()
                    if (principal != null && name != null) {
//                        iUserService.findByName(name)
                        if (principal.name == name) {
                            call.respondText {
                                principal.name
                            }
                            //todo
                        }
                    }
                    call.respondText {
                        "hello $name !!"
                    }
                }
            }

            authenticate(tokenAuth) {
                get("/admin"){
                    println("cccccccccccc "+call.principal<UserIdPrincipal>())
                    println("cccccccccccc "+call.principal<UserSession>())

                    return@get call.respondText {
                        "you alredy in admin !! hello " +
                                call.principal<UserIdPrincipal>()?.name.toString()
                    }
                }
            }
        }
    }
}
