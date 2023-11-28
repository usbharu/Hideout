package dev.usbharu.hideout.activitypub.domain.model

import dev.usbharu.hideout.activitypub.domain.model.objects.Object

open class Key : Object, HasId {
    var owner: String? = null
    var publicKeyPem: String? = null
    override val id: String

    constructor(
        type: List<String>,
        id: String,
        owner: String?,
        publicKeyPem: String?
    ) : super(
        type = add(list = type, type = "Key")
    ) {
        this.owner = owner
        this.publicKeyPem = publicKeyPem
        this.id = id
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Key

        if (owner != other.owner) return false
        if (publicKeyPem != other.publicKeyPem) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (owner?.hashCode() ?: 0)
        result = 31 * result + (publicKeyPem?.hashCode() ?: 0)
        result = 31 * result + id.hashCode()
        return result
    }

    override fun toString(): String = "Key(owner=$owner, publicKeyPem=$publicKeyPem, id='$id') ${super.toString()}"
}
