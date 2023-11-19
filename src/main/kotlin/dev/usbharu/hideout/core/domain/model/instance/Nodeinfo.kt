package dev.usbharu.hideout.core.domain.model.instance

class Nodeinfo {

    var links: List<Links> = emptyList()

    private constructor()
}

class Links {
    var rel: String? = null
    var href: String? = null

    private constructor()
}
