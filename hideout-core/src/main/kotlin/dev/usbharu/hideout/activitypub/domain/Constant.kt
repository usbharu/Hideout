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

package dev.usbharu.hideout.activitypub.domain

import dev.usbharu.hideout.activitypub.domain.model.StringOrObject

object Constant {
    val context = listOf(
        StringOrObject("https://www.w3.org/ns/activitystreams"),
        StringOrObject("https://w3id.org/security/v1"),
        StringOrObject(
            mapOf(
                "manuallyApprovesFollowers" to "as:manuallyApprovesFollowers",
                "sensitive" to "as:sensitive",
                "Hashtag" to "as:Hashtag",
                "quoteUrl" to "as:quoteUrl",
                "toot" to "http://joinmastodon.org/ns#",
                "Emoji" to "toot:Emoji",
                "featured" to "toot:featured",
                "discoverable" to "toot:discoverable",
                "schema" to "http://schema.org#",
                "PropertyValue" to "schema:PropertyValue",
                "value" to "schema:value",
            )
        )
    )
}