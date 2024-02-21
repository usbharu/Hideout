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

package dev.usbharu.hideout.core.infrastructure.springframework.httpsignature

import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.util.Base64Util
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*

@Component
class HttpSignatureHeaderChecker(private val applicationConfig: ApplicationConfig) {
    fun checkDate(date: String) {
        val from = Instant.from(dateFormat.parse(date))

        if (from.isAfter(Instant.now()) || from.isBefore(Instant.now().minusSeconds(86400))) {
            throw IllegalArgumentException("未来")
        }
    }

    fun checkHost(host: String) {
        if (applicationConfig.url.host.equals(host, true).not()) {
            throw IllegalArgumentException("ホスト名が違う")
        }
    }

    fun checkDigest(byteArray: ByteArray, digest: String) {
        val find = regex.find(digest)
        val sha256 = MessageDigest.getInstance("SHA-256")

        val other = find?.groups?.get(2)?.value.orEmpty()

        if (Base64Util.encode(sha256.digest(byteArray)).equals(other, true).not()) {
            throw IllegalArgumentException("リクエストボディが違う")
        }
    }

    companion object {
        private val dateFormat = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)
        private val regex = Regex("^([a-zA-Z0-9\\-]+)=(.+)$")
    }
}
