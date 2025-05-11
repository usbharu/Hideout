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

import dev.usbharu.hideout.core.domain.model.emoji.CustomEmojiId

class PostContent {

    val text: String
    val content: String
    val emojiIds: List<CustomEmojiId>

    constructor(text: String, content: String, emojiIds: List<CustomEmojiId>) {
        this.text = text.take(TEXT_LENGTH)
        this.content = content.take(CONTENT_LENGTH)
        this.emojiIds = emojiIds.distinct()
    }

    companion object {
        val empty = PostContent("", "", emptyList())
        const val CONTENT_LENGTH = 5000
        const val TEXT_LENGTH = 3000
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PostContent

        if (text != other.text) return false
        if (content != other.content) return false
        if (emojiIds != other.emojiIds) return false

        return true
    }

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + emojiIds.hashCode()
        return result
    }

    override fun toString(): String {
        return "PostContent(" +
                "text='$text', " +
                "content='$content', " +
                "emojiIds=$emojiIds" +
                ")"
    }
}
