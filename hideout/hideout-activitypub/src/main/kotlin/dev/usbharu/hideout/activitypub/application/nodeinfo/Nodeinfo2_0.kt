package dev.usbharu.hideout.activitypub.application.nodeinfo

@Suppress("ClassName")
data class Nodeinfo2_0(
    val version: String = "2,0",
    val software: Map<String, String>,
    val protocol: List<String>,
    val usage: NodeinfoUsage,
    val openRegistration: Boolean,
    val metadata: Map<String, Any>,
)

data class NodeinfoUsage(
    val users: Map<String, Long>,
    val localPosts: Long,
)
