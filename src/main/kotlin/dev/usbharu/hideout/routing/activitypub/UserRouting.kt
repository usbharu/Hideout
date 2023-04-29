package dev.usbharu.hideout.routing.activitypub

import dev.usbharu.hideout.exception.ParameterNotExistException
import dev.usbharu.hideout.plugins.respondAp
import dev.usbharu.hideout.service.activitypub.ActivityPubUserService
import dev.usbharu.hideout.service.impl.IUserService
import dev.usbharu.hideout.util.HttpUtil.Activity
import dev.usbharu.hideout.util.HttpUtil.JsonLd
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.usersAP(activityPubUserService: ActivityPubUserService, userService: IUserService) {
    route("/users/{name}") {
        createChild(ContentTypeRouteSelector(ContentType.Application.Activity, ContentType.Application.JsonLd)).handle {
            val name =
                call.parameters["name"] ?: throw ParameterNotExistException("Parameter(name='name') does not exist.")
            val person = activityPubUserService.getPersonByName(name)
            return@handle call.respondAp(
                person,
                HttpStatusCode.OK
            )
        }
        get {
            val userEntity = userService.findByNameLocalUser(call.parameters["name"]!!)
            call.respondText(userEntity.toString() + "\n" + userService.findFollowersById(userEntity.id))
        }
    }
}

class ContentTypeRouteSelector(private vararg val contentType: ContentType) : RouteSelector() {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {

        context.call.application.log.debug("Accept: ${context.call.request.accept()}")
        val requestContentType =
            ContentType.parse(context.call.request.accept() ?: return RouteSelectorEvaluation.FailedParameter)

        return if (contentType.find { contentType: ContentType -> contentType.match(requestContentType) } != null) {
            RouteSelectorEvaluation.Constant
        } else {
            RouteSelectorEvaluation.FailedParameter
        }
    }

}
