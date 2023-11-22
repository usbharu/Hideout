package dev.usbharu.hideout.core.domain.model.tmp

import java.time.Instant

data class User(
    val id: UserId,
    val name: String,
    val domain: String,
    val screenName: String,
    val description: String,
    val url: String,
    val publicKey: String,
    val privateKey: String?,
    val inbox: String,
    val outbox: String,
    val keyId: String,
    val followers: String,
    val following: String,
    val instance: InstanceId,
    val createdAt: Instant,
    val updatedAt: Instant
)
