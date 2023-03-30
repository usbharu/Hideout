package dev.usbharu.hideout.webfinger

class WebFinger(val subject: String, val aliases: List<String>, val links: List<Link>) {
    class Link(val rel: String, val type: String?, val href: String?, val template: String)
}
