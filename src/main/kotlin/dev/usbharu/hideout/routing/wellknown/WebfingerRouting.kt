package dev.usbharu.hideout.routing.wellknown

import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.domain.model.wellknown.WebFinger
import dev.usbharu.hideout.exception.IllegalParameterException
import dev.usbharu.hideout.exception.ParameterNotExistException
import dev.usbharu.hideout.service.api.WebFingerApiService
import dev.usbharu.hideout.util.HttpUtil.Activity
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

@Deprecated("Ktor is deprecated")
fun Routing.webfinger(webFingerApiService: WebFingerApiService) {
    route("/.well-known/webfinger") {
        get {
            val acct = call.request.queryParameters["resource"]?.decodeURLPart()
                ?: throw ParameterNotExistException("Parameter(name='resource') does not exist.")

            if (acct.startsWith("acct:").not()) {
                throw IllegalParameterException("Parameter(name='resource') is not start with 'acct:'.")
            }

            val accountName = acct.substringBeforeLast("@")
                .substringAfter("acct:")
                .trimStart('@')

            val userEntity = webFingerApiService.findByNameAndDomain(accountName, Config.configData.domain)

            val webFinger = WebFinger(
                subject = acct,
                links = listOf(
                    WebFinger.Link(
                        rel = "self",
                        type = ContentType.Application.Activity.toString(),
                        href = "${Config.configData.url}/users/${userEntity.name}"
                    )
                )
            )

            return@get call.respond(webFinger)
        }
    }
}
