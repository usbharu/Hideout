package dev.usbharu.hideout.core.domain.model.meta

import java.util.*

data class Jwt(val kid: UUID, val privateKey: String, val publicKey: String)
