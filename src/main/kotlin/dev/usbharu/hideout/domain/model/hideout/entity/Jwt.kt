package dev.usbharu.hideout.domain.model.hideout.entity

import java.util.*

data class Jwt(val kid: UUID, val privateKey: String, val publicKey: String)
