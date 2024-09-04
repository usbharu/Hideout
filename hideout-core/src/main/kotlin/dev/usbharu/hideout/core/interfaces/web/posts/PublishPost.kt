package dev.usbharu.hideout.core.interfaces.web.posts

data class PublishPost(
    var status: String? = null,
    var overview: String? = null,
    var visibility: String = "PUBLIC",
    var reply_to: Long? = null,
    var repost: Long? = null
)
