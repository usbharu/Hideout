package dev.usbharu.hideout.routing

import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.exception.UserNotFoundException
import dev.usbharu.hideout.service.UserService
import dev.usbharu.hideout.util.HttpUtil.Activity
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.intellij.lang.annotations.Language

fun Application.wellKnown(userService: UserService) {
    routing {
        route("/.well-known") {
            get("/host-meta") {
                //language=XML
                val xml = """
                    <?xml version="1.0" encoding="UTF-8"?><XRD xmlns="http://docs.oasis-open.org/ns/xri/xrd-1.0"><Link rel="lrdd" type="application/xrd+xml" template="${Config.configData.hostname}/.well-known/webfinger?resource={uri}"/></XRD>
                """.trimIndent()
                return@get call.respondText(
                    contentType = ContentType("application", "xrd+xml"),
                    status = HttpStatusCode.OK,
                    text = xml
                )
            }

            get("/host-meta.json") {
                @Language("JSON") val json = """
                    {
                      "links": [
                        {
                          "rel": "lrdd",
                          "type": "application/jrd+json",
                          "template": "${Config.configData.hostname}/.well-known/webfinger?resource={uri}"
                        }
                      ]
                    }
                """.trimIndent()
                return@get call.respondText(
                    contentType = ContentType("application", "xrd+json"),
                    status = HttpStatusCode.OK,
                    text = json
                )
            }

            get("/webfinger") {
                val uri = call.request.queryParameters["resource"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest
                )

                if (uri.startsWith("acct:")) {
                    return@get call.respond(HttpStatusCode.BadRequest)
                }
                val accountName = uri.substringBeforeLast("@")
                val userEntity = userService.findByName(accountName)

                return@get call.respond(WebFingerResource(
                    subject = userEntity.name,
                    listOf(
                        WebFingerResource.Link(
                            rel ="self",
                            type = ContentType.Application.Activity.toString(),
                            href = "${Config.configData.hostname}/users/${userEntity.name}"
                        )
                    )
                ))
            }

        }
    }
}

@Serializable
data class WebFingerResource(val subject:String,val links:List<Link>){
    @Serializable
    data class Link(val rel:String,val type:String,val href:String)
}
