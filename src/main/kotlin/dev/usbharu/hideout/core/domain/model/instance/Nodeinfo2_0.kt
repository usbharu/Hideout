@file:Suppress("Filename")

package dev.usbharu.hideout.core.domain.model.instance

@Suppress("ClassNaming")
class Nodeinfo2_0 {
    var metadata: Metadata? = null
    var software: Software? = null

    constructor()
}

class Metadata {
    var nodeName: String? = null
    var nodeDescription: String? = null

    constructor()
}

class Software {
    var name: String? = null
    var version: String? = null

    constructor()
}
