package dev.usbharu.hideout.activitypub.domain.model.webfinger

data class WebFinger(val subject: String, val links: List<Link>) {
    data class Link(val rel: String, val type: String, val href: String)
}
