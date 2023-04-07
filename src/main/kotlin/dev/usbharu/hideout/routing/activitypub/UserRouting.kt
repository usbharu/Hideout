package dev.usbharu.hideout.routing.activitypub

import dev.usbharu.hideout.service.activitypub.ActivityPubService
import dev.usbharu.hideout.util.HttpUtil.Activity
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.usersAP(activityPubService: ActivityPubService){
    route("/users/{name}"){
        createChild(ContentTypeRouteSelector(ContentType.Application.Activity)).handle {

            call.respond(HttpStatusCode.NotImplemented)
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
