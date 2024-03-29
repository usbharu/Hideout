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

package dev.usbharu.hideout.core.domain.model.emoji

import java.time.Instant

sealed class Emoji {
    abstract val domain: String
    abstract val name: String

    @Suppress("FunctionMinLength")
    abstract fun id(): String
    override fun toString(): String {
        return "Emoji(" +
            "domain='$domain', " +
            "name='$name'" +
            ")"
    }
}

data class CustomEmoji(
    val id: Long,
    override val name: String,
    override val domain: String,
    val instanceId: Long?,
    val url: String,
    val category: String?,
    val createdAt: Instant
) : Emoji() {
    override fun id(): String = id.toString()
}

data class UnicodeEmoji(
    override val name: String
) : Emoji() {
    override val domain: String = "unicode.org"
    override fun id(): String = name
}
