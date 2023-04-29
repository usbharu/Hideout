package dev.usbharu.hideout.domain.model.ap

open class Person : Object {
    var preferredUsername: String? = null
    var summary: String? = null
    var inbox: String? = null
    var outbox: String? = null
    private var url: String? = null
    private var icon: Image? = null
    var publicKey: Key? = null

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
        publicKey: Key?
    ) : super(add(type, "Person"), name, id = id) {
        this.preferredUsername = preferredUsername
        this.summary = summary
        this.inbox = inbox
        this.outbox = outbox
        this.url = url
        this.icon = icon
        this.publicKey = publicKey
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Person) return false

        if (id != other.id) return false
        if (preferredUsername != other.preferredUsername) return false
        if (summary != other.summary) return false
        if (inbox != other.inbox) return false
        if (outbox != other.outbox) return false
        if (url != other.url) return false
        if (icon != other.icon) return false
        return publicKey == other.publicKey
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (preferredUsername?.hashCode() ?: 0)
        result = 31 * result + (summary?.hashCode() ?: 0)
        result = 31 * result + (inbox?.hashCode() ?: 0)
        result = 31 * result + (outbox?.hashCode() ?: 0)
        result = 31 * result + (url?.hashCode() ?: 0)
        result = 31 * result + (icon?.hashCode() ?: 0)
        result = 31 * result + (publicKey?.hashCode() ?: 0)
        return result
    }
}
