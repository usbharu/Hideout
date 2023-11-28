package dev.usbharu.hideout.activitypub.domain.model

import dev.usbharu.hideout.activitypub.domain.model.objects.Object

open class Person
@Suppress("LongParameterList")
constructor(
    type: List<String> = emptyList(),
    override val name: String,
    override val id: String,
    var preferredUsername: String?,
    var summary: String?,
    var inbox: String,
    var outbox: String,
    var url: String,
    private var icon: Image?,
    var publicKey: Key?,
    var endpoints: Map<String, String> = emptyMap(),
    var followers: String?,
    var following: String?
) : Object(add(type, "Person")), HasId, HasName {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Person) return false
        if (!super.equals(other)) return false

        if (preferredUsername != other.preferredUsername) return false
        if (summary != other.summary) return false
        if (inbox != other.inbox) return false
        if (outbox != other.outbox) return false
        if (url != other.url) return false
        if (icon != other.icon) return false
        if (publicKey != other.publicKey) return false
        if (endpoints != other.endpoints) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (preferredUsername?.hashCode() ?: 0)
        result = 31 * result + (summary?.hashCode() ?: 0)
        result = 31 * result + inbox.hashCode()
        result = 31 * result + outbox.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + (icon?.hashCode() ?: 0)
        result = 31 * result + (publicKey?.hashCode() ?: 0)
        result = 31 * result + endpoints.hashCode()
        return result
    }
}
