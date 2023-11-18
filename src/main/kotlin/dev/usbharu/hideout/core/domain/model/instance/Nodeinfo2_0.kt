package dev.usbharu.hideout.core.domain.model.instance

class Nodeinfo2_0 {
    var metadata: Metadata? = null
    var software: Software? = null

    protected constructor()
}

class Metadata {
    var nodeName: String? = null
    var nodeDescription: String? = null

    protected constructor()
}

class Software {
    var name: String? = null
    var version: String? = null

    protected constructor()
}
