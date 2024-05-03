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

package dev.usbharu.hideout.core.domain.model.timeline

import dev.usbharu.hideout.core.domain.model.post.Visibility
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document

@Document
@CompoundIndex(def = "{'userId':1,'timelineId':1,'postId':1}", unique = true)
data class Timeline(
    @Id
    val id: Long,
    val userId: Long,
    val timelineId: Long,
    val postId: Long,
    val postActorId: Long,
    val createdAt: Long,
    val replyId: Long?,
    val repostId: Long?,
    val visibility: Visibility,
    val sensitive: Boolean,
    val isLocal: Boolean,
    val isPureRepost: Boolean = false,
    val mediaIds: List<Long> = emptyList(),
    val emojiIds: List<Long> = emptyList()
)
