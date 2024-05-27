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

package dev.usbharu.hideout.core.infrastructure.factory

import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.actor.ActorDescription
import dev.usbharu.hideout.core.domain.model.emoji.CustomEmojiRepository
import dev.usbharu.hideout.core.domain.model.emoji.EmojiId
import org.springframework.stereotype.Component

@Component
class ActorDescriptionFactoryImpl(
    private val applicationConfig: ApplicationConfig,
    private val emojiRepository: CustomEmojiRepository,
) : ActorDescription.ActorDescriptionFactory() {
    val regex = Regex(":(w+):")
    suspend fun create(description: String): ActorDescription {
        val findAll = regex.findAll(description)

        val emojis =
            emojiRepository.findByNamesAndDomain(
                findAll.map { it.groupValues[1] }.toList(),
                applicationConfig.url.host
            )
        return create(description, emojis.map { EmojiId(it.id) })
    }
}