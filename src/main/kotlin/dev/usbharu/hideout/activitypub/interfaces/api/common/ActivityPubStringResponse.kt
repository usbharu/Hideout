package dev.usbharu.hideout.activitypub.interfaces.api.common

import dev.usbharu.hideout.activitypub.domain.model.JsonLd
import dev.usbharu.hideout.util.HttpUtil.Activity
import io.ktor.http.*

sealed class ActivityPubResponse(
    val httpStatusCode: HttpStatusCode,
    val contentType: ContentType = ContentType.Application.Activity
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ActivityPubResponse) return false

        if (httpStatusCode != other.httpStatusCode) return false
        if (contentType != other.contentType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = httpStatusCode.hashCode()
        result = 31 * result + contentType.hashCode()
        return result
    }

    override fun toString(): String = "ActivityPubResponse(httpStatusCode=$httpStatusCode, contentType=$contentType)"
}

class ActivityPubStringResponse(
    httpStatusCode: HttpStatusCode = HttpStatusCode.OK,
    val message: String,
    contentType: ContentType = ContentType.Application.Activity
) : ActivityPubResponse(httpStatusCode, contentType) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ActivityPubStringResponse) return false

        if (message != other.message) return false

        return true
    }

    override fun hashCode(): Int = message.hashCode()

    override fun toString(): String = "ActivityPubStringResponse(message='$message') ${super.toString()}"
}

class ActivityPubObjectResponse(
    httpStatusCode: HttpStatusCode = HttpStatusCode.OK,
    val message: JsonLd,
    contentType: ContentType = ContentType.Application.Activity
) : ActivityPubResponse(httpStatusCode, contentType) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ActivityPubObjectResponse) return false

        if (message != other.message) return false

        return true
    }

    override fun hashCode(): Int = message.hashCode()

    override fun toString(): String = "ActivityPubObjectResponse(message=$message) ${super.toString()}"
}
