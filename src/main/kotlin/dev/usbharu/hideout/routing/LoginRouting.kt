package dev.usbharu.hideout.routing

import dev.usbharu.hideout.plugins.UserSession
import dev.usbharu.hideout.plugins.tokenAuth
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun Application.login(){
    routing {
        authenticate(tokenAuth) {
            post("/login") {
                println("aaaaaaaaaaaaaaaaaaaaa")
                val principal = call.principal<UserIdPrincipal>()
//                call.sessions.set(UserSession(principal!!.name))
                call.respondRedirect("/users/${principal!!.name}")
            }
        }

        get("/login"){
            call.respondText(contentType = ContentType.Text.Html) {

                //language=HTML
                """
                    <html>
                    <head>
                    
                    </head>
                    <body>
                    <h2>login</h2>
                    <form method='POST' action=''>
                    <input type='text' name='username' value=''>
                    <input type='password' name='password'>
                    <input type='submit'>
                    </form>
                    </body>
                    </html>                    
                """.trimIndent()
            }
        }
    }
}
