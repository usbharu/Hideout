package dev.usbharu.hideout.routing.activitypub

import dev.usbharu.hideout.exception.ParameterNotExistException
import dev.usbharu.hideout.plugins.respondAp
import dev.usbharu.hideout.service.activitypub.ActivityPubUserService
import dev.usbharu.hideout.service.user.IUserService
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
            call.application.log.debug("Signature: ${call.request.header("Signature")}")
            call.application.log.debug("Authorization: ${call.request.header("Authorization")}")
            val name =
                call.parameters["name"] ?: throw ParameterNotExistException("Parameter(name='name') does not exist.")
            val person = activityPubUserService.getPersonByName(name)
            return@handle call.respondAp(
                person,
                HttpStatusCode.OK
            )
        }
        get {
            val userEntity = userService.findByNameLocalUser(
                call.parameters["name"] ?: throw ParameterNotExistException("Parameter(name='name') does not exist.")
            )
            call.respondText(userEntity.toString() + "\n" + userService.findFollowersById(userEntity.id))
        }
    }
}

class ContentTypeRouteSelector(private vararg val contentType: ContentType) : RouteSelector() {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        context.call.application.log.debug("Accept: ${context.call.request.accept()}")
        val requestContentType = context.call.request.accept() ?: return RouteSelectorEvaluation.FailedParameter
        return if (requestContentType.split(",")
            .any { contentType.any { contentType -> contentType.match(it) } }
        ) {
            RouteSelectorEvaluation.Constant
        } else {
            RouteSelectorEvaluation.FailedParameter
        }
    }
}
