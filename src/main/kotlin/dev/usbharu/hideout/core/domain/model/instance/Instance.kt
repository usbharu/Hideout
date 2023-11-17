package dev.usbharu.hideout.core.domain.model.instance

data class Instance(
    val id: Long,
    val name: String,
    val description: String,
    val url: String,
    val iconUrl: String,
    val sharedInbox: String,
    val software: String,
    val version: String,
    val isBlocked: Boolean,
    val isMuting: Boolean,
    val moderationNote: String
)
