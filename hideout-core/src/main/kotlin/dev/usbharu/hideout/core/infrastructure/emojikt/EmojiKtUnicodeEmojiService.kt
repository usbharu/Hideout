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

package dev.usbharu.hideout.core.infrastructure.emojikt

import Emojis
import dev.usbharu.hideout.core.domain.service.emoji.UnicodeEmojiService
import org.springframework.stereotype.Service

@Service
class EmojiKtUnicodeEmojiService : UnicodeEmojiService {
    override fun isUnicodeEmoji(emoji: String): Boolean = Emojis.allEmojis.singleOrNull { it.char == emoji } != null
}
