package dev.usbharu.hideout.util

import dev.usbharu.hideout.core.domain.model.user.Acct

object AcctUtil {
    fun parse(string: String): Acct {
        if (string.isBlank()) {
            throw IllegalArgumentException("Invalid acct.(Blank)")
        }
        return when (string.count { c -> c == '@' }) {
            0 -> {
                Acct(string)
            }

            1 -> {
                if (string.startsWith("@")) {
                    Acct(string.substring(1 until string.length))
                } else {
                    Acct(string.substringBefore("@"), string.substringAfter("@"))
                }
            }

            2 -> {
                if (string.startsWith("@")) {
                    val substring = string.substring(1 until string.length)
                    val userName = substring.substringBefore("@")
                    val domain = substring.substringAfter("@")
                    Acct(
                        userName,
                        domain.ifBlank { null }
                    )
                } else {
                    throw IllegalArgumentException("Invalid acct.(@ are in the wrong position)")
                }
            }

            else -> {
                throw IllegalArgumentException("Invalid acct. (Too many @)")
            }
        }
    }
}
