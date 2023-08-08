package dev.usbharu.hideout.domain.model.hideout.dto

data class ReactionResponse(
    val reaction: String,
    val isUnicodeEmoji: Boolean = true,
    val iconUrl: String,
    val accounts: List<Account>
)

data class Account(val screenName: String, val iconUrl: String, val url: String)
