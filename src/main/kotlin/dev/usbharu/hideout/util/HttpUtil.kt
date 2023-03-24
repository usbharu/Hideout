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
        if (subType == "ld+json") {
            return true

        }
        return false
    }

    val ContentType.Application.Activity: ContentType
        get() = ContentType("application","activity+json")
//    fun
}
