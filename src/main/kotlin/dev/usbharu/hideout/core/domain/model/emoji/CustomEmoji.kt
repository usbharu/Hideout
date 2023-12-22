package dev.usbharu.hideout.core.domain.model.emoji

import java.time.Instant

sealed class Emoji {
    abstract val domain: String
    abstract val name: String
    abstract fun id(): String
    override fun toString(): String {
        return "Emoji(" +
                "domain='$domain', " +
                "name='$name'" +
                ")"
    }
}

data class CustomEmoji(
    val id: Long,
    override val name: String,
    override val domain: String,
    val instanceId: Long?,
    val url: String,
    val category: String?,
    val createdAt: Instant
) : Emoji() {
    override fun id(): String {
        return id.toString()
    }
}

data class UnicodeEmoji(
    override val name: String
) : Emoji() {
    override val domain: String = "unicode.org"
    override fun id(): String {
        return name
    }
}
