package dev.usbharu.hideout.util

import io.ktor.http.*

object HttpUtil {
    val Activity: ContentType
        get() = ContentType("application", "activity+json")

    val JsonLd: ContentType
        get() {
            return ContentType(
                contentType = "application",
                contentSubtype = "ld+json",
                parameters = listOf(HeaderValueParam("profile", "https://www.w3.org/ns/activitystreams"))
            )
        }

    fun isContentTypeOfActivityPub(
        contentType: String,
        subType: String
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
            contentType.contentSubtype
        )
    }
//    fun
}
