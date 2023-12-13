package dev.usbharu.hideout.activitypub.domain.model

import dev.usbharu.hideout.activitypub.domain.model.objects.Object

open class Person
@Suppress("LongParameterList")
constructor(
    type: List<String> = emptyList(),
    val name: String?,
    override val id: String,
    var preferredUsername: String,
    var summary: String?,
    var inbox: String,
    var outbox: String,
    var url: String,
    private var icon: Image?,
    var publicKey: Key,
    var endpoints: Map<String, String> = emptyMap(),
    var followers: String?,
    var following: String?,
    val manuallyApprovesFollowers: Boolean? = false
) : Object(add(type, "Person")), HasId {

    @Suppress("CyclomaticComplexMethod", "CognitiveComplexMethod")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Person

        if (name != other.name) return false
        if (id != other.id) return false
        if (preferredUsername != other.preferredUsername) return false
        if (summary != other.summary) return false
        if (inbox != other.inbox) return false
        if (outbox != other.outbox) return false
        if (url != other.url) return false
        if (icon != other.icon) return false
        if (publicKey != other.publicKey) return false
        if (endpoints != other.endpoints) return false
        if (followers != other.followers) return false
        if (following != other.following) return false
        if (manuallyApprovesFollowers != other.manuallyApprovesFollowers) return false

        return true
    }

    @Suppress("CyclomaticComplexMethod")
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + (preferredUsername?.hashCode() ?: 0)
        result = 31 * result + (summary?.hashCode() ?: 0)
        result = 31 * result + inbox.hashCode()
        result = 31 * result + outbox.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + (icon?.hashCode() ?: 0)
        result = 31 * result + publicKey.hashCode()
        result = 31 * result + endpoints.hashCode()
        result = 31 * result + (followers?.hashCode() ?: 0)
        result = 31 * result + (following?.hashCode() ?: 0)
        result = 31 * result + manuallyApprovesFollowers.hashCode()
        return result
    }

    override fun toString(): String {
        return "Person(" +
                "name='$name', " +
                "id='$id', " +
                "preferredUsername=$preferredUsername, " +
                "summary=$summary, " +
                "inbox='$inbox', " +
                "outbox='$outbox', " +
                "url='$url', " +
                "icon=$icon, " +
                "publicKey=$publicKey, " +
                "endpoints=$endpoints, " +
                "followers=$followers, " +
                "following=$following, " +
                "manuallyApprovesFollowers=$manuallyApprovesFollowers" +
                ")" +
                " ${super.toString()}"
    }
}
