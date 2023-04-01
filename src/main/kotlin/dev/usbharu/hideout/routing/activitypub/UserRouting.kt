package dev.usbharu.hideout.routing.activitypub

import dev.usbharu.hideout.util.HttpUtil.Activity
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Routing.users(){
    route("/users/{name}"){
        createChild(ContentTypeRouteSelector(ContentType.Application.Activity)).handle {
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
