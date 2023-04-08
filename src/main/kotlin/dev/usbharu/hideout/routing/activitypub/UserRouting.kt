package dev.usbharu.hideout.routing.activitypub

import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.exception.ParameterNotExistException
import dev.usbharu.hideout.service.activitypub.ActivityPubUserService
import dev.usbharu.hideout.util.HttpUtil.Activity
import dev.usbharu.hideout.util.HttpUtil.JsonLd
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.usersAP(activityPubUserService: ActivityPubUserService) {
    route("/users/{name}") {
        createChild(ContentTypeRouteSelector(ContentType.Application.Activity, ContentType.Application.JsonLd)).handle {
            val name =
                call.parameters["name"] ?: throw ParameterNotExistException("Parameter(name='name') does not exist.")
            val person = activityPubUserService.getPersonByName(name)
            call.response.header("Content-Type", ContentType.Application.Activity.toString())
            call.respond(HttpStatusCode.OK, Config.configData.objectMapper.writeValueAsString(person))
        }
    }
}

class ContentTypeRouteSelector(private vararg val contentType: ContentType) : RouteSelector() {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {

        val requestContentType =
            ContentType.parse(context.call.request.accept() ?: return RouteSelectorEvaluation.FailedParameter)
        return if (contentType.any { contentType -> contentType.match(requestContentType) }) {
            RouteSelectorEvaluation.Constant
        } else {
            RouteSelectorEvaluation.FailedParameter
        }
    }

}
