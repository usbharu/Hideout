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

package dev.usbharu.hideout.core.domain.model.reaction

import dev.usbharu.hideout.core.domain.model.emoji.Emoji
import org.springframework.stereotype.Repository

@Repository
@Suppress("FunctionMaxLength", "TooManyFunctions")
interface ReactionRepository {
    suspend fun generateId(): Long
    suspend fun save(reaction: Reaction): Reaction
    suspend fun delete(reaction: Reaction): Reaction
    suspend fun deleteByPostId(postId: Long): Int
    suspend fun deleteByActorId(actorId: Long): Int
    suspend fun deleteByPostIdAndActorId(postId: Long, actorId: Long)
    suspend fun deleteByPostIdAndActorIdAndEmoji(postId: Long, actorId: Long, emoji: Emoji)
    suspend fun findById(id: Long): Reaction?
    suspend fun findByPostId(postId: Long): List<Reaction>
    suspend fun findByPostIdAndActorIdAndEmojiId(postId: Long, actorId: Long, emojiId: Long): Reaction?
    suspend fun existByPostIdAndActorIdAndEmojiId(postId: Long, actorId: Long, emojiId: Long): Boolean
    suspend fun existByPostIdAndActorIdAndUnicodeEmoji(postId: Long, actorId: Long, unicodeEmoji: String): Boolean
    suspend fun existByPostIdAndActorIdAndEmoji(postId: Long, actorId: Long, emoji: Emoji): Boolean
    suspend fun existByPostIdAndActor(postId: Long, actorId: Long): Boolean
    suspend fun findByPostIdAndActorId(postId: Long, actorId: Long): List<Reaction>
}
