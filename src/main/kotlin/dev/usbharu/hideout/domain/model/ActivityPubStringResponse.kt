package dev.usbharu.hideout.domain.model

import dev.usbharu.hideout.domain.model.ap.JsonLd
import dev.usbharu.hideout.util.HttpUtil.Activity
import io.ktor.http.*

sealed class ActivityPubResponse(
    val httpStatusCode: HttpStatusCode,
    val contentType: ContentType = ContentType.Application.Activity
)

class ActivityPubStringResponse(
    httpStatusCode: HttpStatusCode = HttpStatusCode.OK,
    val message: String,
    contentType: ContentType = ContentType.Application.Activity
) :
    ActivityPubResponse(httpStatusCode, contentType)

class ActivityPubObjectResponse(
    httpStatusCode: HttpStatusCode = HttpStatusCode.OK,
    val message: JsonLd,
    contentType: ContentType = ContentType.Application.Activity
) :
    ActivityPubResponse(httpStatusCode, contentType)
