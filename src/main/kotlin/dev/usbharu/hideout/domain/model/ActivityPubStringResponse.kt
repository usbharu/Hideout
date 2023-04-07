package dev.usbharu.hideout.domain.model

import dev.usbharu.hideout.ap.JsonLd
import io.ktor.http.*

sealed class ActivityPubResponse(val httpStatusCode: HttpStatusCode)
class ActivityPubStringResponse(httpStatusCode: HttpStatusCode, val message: String) :
    ActivityPubResponse(httpStatusCode)

class ActivityPubObjectResponse(httpStatusCode: HttpStatusCode, val message: JsonLd) :
    ActivityPubResponse(httpStatusCode)
