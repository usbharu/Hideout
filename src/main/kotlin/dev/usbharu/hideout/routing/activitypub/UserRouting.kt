package dev.usbharu.hideout.routing.activitypub

import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.exception.ParameterNotExistException
import dev.usbharu.hideout.service.activitypub.ActivityPubService
import dev.usbharu.hideout.service.activitypub.ActivityPubUserService
import dev.usbharu.hideout.util.HttpUtil.Activity
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.usersAP(activityPubUserService:ActivityPubUserService){
    route("/users/{name}"){
        createChild(ContentTypeRouteSelector(ContentType.Application.Activity)).handle {
            val name = call.parameters["name"] ?: throw ParameterNotExistException("Parameter(name='name') does not exist.")
            val person = activityPubUserService.getPersonByName(name)
            call.response.header("Content-Type", ContentType.Application.Activity.toString())
            call.respond(HttpStatusCode.OK,Config.configData.objectMapper.writeValueAsString(person))
        }
    }
}

class ContentTypeRouteSelector(private val contentType: ContentType) : RouteSelector() {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        return if (ContentType.parse(context.call.request.accept() ?: return RouteSelectorEvaluation.FailedParameter) == contentType) {
            RouteSelectorEvaluation.Constant
        } else {
            RouteSelectorEvaluation.FailedParameter
        }
    }

}
