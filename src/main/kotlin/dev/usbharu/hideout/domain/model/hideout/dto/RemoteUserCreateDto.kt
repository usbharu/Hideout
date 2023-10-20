package dev.usbharu.hideout.domain.model.hideout.dto

data class RemoteUserCreateDto(
    val name: String,
    val domain: String,
    val screenName: String,
    val description: String,
    val inbox: String,
    val outbox: String,
    val url: String,
    val publicKey: String,
    val keyId: String,
    val followers: String?,
    val following: String?
)
