package dev.usbharu.hideout.routing

import dev.usbharu.hideout.domain.model.hideout.dto.UserCreateDto
import dev.usbharu.hideout.service.impl.IUserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.register(userService: IUserService) {
    routing {
        get("/register") {
            val principal = call.principal<UserIdPrincipal>()
            if (principal != null) {
                call.respondRedirect("/users/${principal.name}")
            }
            call.respondText(ContentType.Text.Html) {
                //language=HTML
                """
                    <html>
                    <head>
                    </head>
                    <body>
                    <form method='post' action=''>
                    <input type='text' name='username' value=''>
                    <input type='password' name='password'>
                    <input type="submit">
                    </form>
                    </body>
                    </html>
                """.trimIndent()
            }
        }
        post("/register") {
            val parameters = call.receiveParameters()
            val password = parameters["password"] ?: return@post call.respondRedirect("/register")
            val username = parameters["username"] ?: return@post call.respondRedirect("/register")
            if (userService.usernameAlreadyUse(username)) {
                return@post call.respondRedirect("/register")
            }
            userService.createLocalUser(UserCreateDto(username, username, "", password))
            call.respondRedirect("/users/$username")
        }
    }
}
