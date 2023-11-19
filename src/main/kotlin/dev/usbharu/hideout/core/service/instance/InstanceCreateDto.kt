package dev.usbharu.hideout.core.service.instance

data class InstanceCreateDto(
    val name: String?,
    val description: String?,
    val url: String,
    val iconUrl: String,
    val sharedInbox: String?,
    val software: String?,
    val version: String?,
)
