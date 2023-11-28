@file:Suppress("Filename")

package dev.usbharu.hideout.core.domain.model.instance

@Suppress("ClassNaming")
class Nodeinfo2_0() {
    var metadata: Metadata? = null
    var software: Software? = null
}

class Metadata() {
    var nodeName: String? = null
    var nodeDescription: String? = null
}

class Software() {
    var name: String? = null
    var version: String? = null
}
