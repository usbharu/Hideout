package dev.usbharu.hideout.core.domain.model.instance

class Nodeinfo private constructor() {

    var links: List<Links> = emptyList()
}

class Links private constructor() {
    var rel: String? = null
    var href: String? = null
}
