package dev.usbharu.hideout.domain.model.wellknown

data class Nodeinfo(
    val links: List<Links>
) {
    data class Links(
        val rel: String,
        val href: String
    )
}
