package dev.usbharu.hideout.activitypub.domain.model

import dev.usbharu.hideout.activitypub.domain.model.objects.Object

open class Person : Object {
    var preferredUsername: String? = null
    var summary: String? = null
    var inbox: String? = null
    var outbox: String? = null
    var url: String? = null
    private var icon: Image? = null
    var publicKey: Key? = null
    var endpoints: Map<String, String> = emptyMap()
    var following: String? = null
    var followers: String? = null

    protected constructor() : super()

    @Suppress("LongParameterList")
    constructor(
        type: List<String> = emptyList(),
        name: String,
        id: String?,
        preferredUsername: String?,
        summary: String?,
        inbox: String?,
        outbox: String?,
        url: String?,
        icon: Image?,
        publicKey: Key?,
        endpoints: Map<String, String> = emptyMap(),
        followers: String?,
        following: String?
    ) : super(add(type, "Person"), name, id = id) {
        this.preferredUsername = preferredUsername
        this.summary = summary
        this.inbox = inbox
        this.outbox = outbox
        this.url = url
        this.icon = icon
        this.publicKey = publicKey
        this.endpoints = endpoints
        this.followers = followers
        this.following = following
    }

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
        result = 31 * result + (inbox?.hashCode() ?: 0)
        result = 31 * result + (outbox?.hashCode() ?: 0)
        result = 31 * result + (url?.hashCode() ?: 0)
        result = 31 * result + (icon?.hashCode() ?: 0)
        result = 31 * result + (publicKey?.hashCode() ?: 0)
        result = 31 * result + endpoints.hashCode()
        return result
    }
}
