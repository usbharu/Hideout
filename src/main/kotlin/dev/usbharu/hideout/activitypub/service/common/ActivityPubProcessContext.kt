package dev.usbharu.hideout.activitypub.service.common

import com.fasterxml.jackson.databind.JsonNode
import dev.usbharu.hideout.activitypub.domain.model.objects.Object
import dev.usbharu.httpsignature.common.HttpRequest
import dev.usbharu.httpsignature.verify.Signature

data class ActivityPubProcessContext<T : Object>(
    val activity: T,
    val jsonNode: JsonNode,
    val httpRequest: HttpRequest,
    val signature: Signature?,
    val isAuthorized: Boolean
)
