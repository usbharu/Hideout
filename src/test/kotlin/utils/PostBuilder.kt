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

package utils

import dev.usbharu.hideout.application.config.CharacterLimit
import dev.usbharu.hideout.application.config.HtmlSanitizeConfig
import dev.usbharu.hideout.application.service.id.TwitterSnowflakeIdGenerateService
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.service.post.DefaultPostContentFormatter
import kotlinx.coroutines.runBlocking
import java.time.Instant

object PostBuilder {

    private val postBuilder =
        Post.PostBuilder(CharacterLimit(), DefaultPostContentFormatter(HtmlSanitizeConfig().policy()))

    private val idGenerator = TwitterSnowflakeIdGenerateService

    fun of(
        id: Long = generateId(),
        userId: Long = generateId(),
        overview: String? = null,
        text: String = "Hello World",
        createdAt: Long = Instant.now().toEpochMilli(),
        visibility: Visibility = Visibility.PUBLIC,
        url: String = "https://example.com/users/$userId/posts/$id"
    ): Post {
        return postBuilder.of(
            id = id,
            actorId = userId,
            overview = overview,
            content = text,
            createdAt = createdAt,
            visibility = visibility,
            url = url,
        )
    }

    private fun generateId(): Long = runBlocking {
        idGenerator.generateId()
    }
}
