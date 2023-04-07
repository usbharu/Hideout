package dev.usbharu.hideout.domain.model

import io.ktor.http.*

data class ActivityPubResponse(val httpStatusCode: HttpStatusCode, val message:String)
