package dev.usbharu.hideout.activitypub.domain.model.nodeinfo

data class Nodeinfo(
    val links: List<Links>
) {
    data class Links(
        val rel: String,
        val href: String
    )
}
