package dev.usbharu.hideout.util

import io.ktor.http.*

object HttpUtil {
    fun isContentTypeOfActivityPub(
        contentType: String,
        subType: String,
        parameter: String
    ): Boolean {
        println("$contentType/$subType $parameter")
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

    val ContentType.Application.Activity: ContentType
        get() = ContentType("application", "activity+json")
//    fun
}
