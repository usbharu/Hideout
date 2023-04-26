package dev.usbharu.hideout.domain.model

import java.time.LocalDateTime

data class User(
    val id:Long,
    val name: String,
    val domain: String,
    val screenName: String,
    val description: String,
    val password:String? = null,
    val inbox: String,
    val outbox: String,
    val url: String,
    val publicKey:String,
    val privateKey:String? = null,
    val createdAt:LocalDateTime
)
