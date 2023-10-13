package dev.usbharu.hideout.service.signature

import java.security.PrivateKey
import java.security.PublicKey

data class Key(
    val keyId: String,
    val privateKey: PrivateKey,
    val publicKey: PublicKey
)
