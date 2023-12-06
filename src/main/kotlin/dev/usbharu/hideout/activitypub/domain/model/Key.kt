package dev.usbharu.hideout.activitypub.domain.model

import dev.usbharu.hideout.activitypub.domain.model.objects.Object

open class Key(
    override val id: String,
    val owner: String,
    val publicKeyPem: String
) : Object(
    type = add(list = emptyList(), type = "Key")
),
    HasId {

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
        result = 31 * result + owner.hashCode()
        result = 31 * result + publicKeyPem.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }

    override fun toString(): String = "Key(owner=$owner, publicKeyPem=$publicKeyPem, id='$id') ${super.toString()}"
}
