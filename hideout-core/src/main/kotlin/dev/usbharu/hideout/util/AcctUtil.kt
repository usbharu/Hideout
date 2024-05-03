/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.usbharu.hideout.util

import dev.usbharu.hideout.core.domain.model.actor.Acct

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
