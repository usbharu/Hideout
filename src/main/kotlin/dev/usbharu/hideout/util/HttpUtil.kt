package dev.usbharu.hideout.util

import io.ktor.http.*

object HttpUtil {
    val ContentType.Application.Activity: ContentType
        get() = ContentType("application", "activity+json")

    val ContentType.Application.JsonLd: ContentType
        get() {
            return ContentType(
                contentType = "application",
                contentSubtype = "ld+json",
                parameters = listOf(HeaderValueParam("profile", "https://www.w3.org/ns/activitystreams"))
            )
        }

    fun isContentTypeOfActivityPub(
        contentType: String,
        subType: String,
        parameter: String
    ): Boolean {
        if (contentType != "application") {
            return false
        }
        if (subType == "activity+json") {
            return true
        }
        return subType == "ld+json"
    }

    fun isContentTypeOfActivityPub(contentType: ContentType): Boolean {
        return isContentTypeOfActivityPub(
            contentType.contentType,
            contentType.contentSubtype,
            contentType.parameter("profile").orEmpty()
        )
    }
//    fun
}
