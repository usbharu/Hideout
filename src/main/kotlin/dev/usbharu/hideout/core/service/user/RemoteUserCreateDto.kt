package dev.usbharu.hideout.core.service.user

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
    val following: String?,
    val sharedInbox: String?,
    val locked: Boolean?
)
