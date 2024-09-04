package dev.usbharu.hideout.core.interfaces.web.posts

data class PublishPost(
    var status: String? = null,
    var overview: String? = null,
    var visibility: String = "PUBLIC",
    var replyTo: Long? = null,
    var repost: Long? = null
)
