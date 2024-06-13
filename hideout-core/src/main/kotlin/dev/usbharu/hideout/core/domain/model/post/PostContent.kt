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

package dev.usbharu.hideout.core.domain.model.post

import dev.usbharu.hideout.core.domain.model.emoji.EmojiId

data class PostContent(val text: String, val content: String, val emojiIds: List<EmojiId>) {

    companion object {
        val empty = PostContent("", "", emptyList())
        val contentLength = 5000
        val textLength = 3000
    }
}
