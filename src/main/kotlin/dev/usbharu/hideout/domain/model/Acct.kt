package dev.usbharu.hideout.domain.model

data class Acct(val username: String, val domain: String? = null, val isRemote: Boolean = domain == null)
