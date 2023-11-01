package dev.usbharu.hideout.activitypub.domain.model

import dev.usbharu.hideout.activitypub.domain.model.`object`.Object

open class Key : Object {
    var owner: String? = null
    var publicKeyPem: String? = null

    protected constructor() : super()
    constructor(
        type: List<String>,
        name: String,
        id: String,
        owner: String?,
        publicKeyPem: String?
    ) : super(
        type = add(list = type, type = "Key"),
        name = name,
        id = id
    ) {
        this.owner = owner
        this.publicKeyPem = publicKeyPem
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Key) return false
        if (!super.equals(other)) return false

        if (owner != other.owner) return false
        return publicKeyPem == other.publicKeyPem
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (owner?.hashCode() ?: 0)
        result = 31 * result + (publicKeyPem?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "Key(owner=$owner, publicKeyPem=$publicKeyPem) ${super.toString()}"
}
