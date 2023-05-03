package dev.usbharu.hideout.util

import java.util.*

object Base64Util {
    fun decode(str: String): ByteArray = Base64.getDecoder().decode(str)

    fun encode(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)
}
