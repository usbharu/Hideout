package dev.usbharu.hideout.core.domain.model.actor

data class Acct(val username: String, val domain: String? = null, val isRemote: Boolean = domain == null)
