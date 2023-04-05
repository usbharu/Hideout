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
            val json = call.receiveText()
            val activityTypes = activityPubService.parseActivity(json)
            activityPubService.processActivity(json,activityTypes)
            call.respond(HttpStatusCode.NotImplemented)
        }
    }
}

class ContentTypeRouteSelector(private val contentType: ContentType) : RouteSelector() {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        return if (context.call.request.contentType() == contentType) {
            RouteSelectorEvaluation.Constant
        } else {
            RouteSelectorEvaluation.Failed
        }
    }

}
